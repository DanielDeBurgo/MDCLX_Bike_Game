package bikeproject.uni.bikeapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.activity_customization.*
import org.json.JSONArray
import java.lang.ref.WeakReference

class CustomizationActivity : AppCompatActivity()
{
    companion object
    {
        private lateinit var activityRef: WeakReference<CustomizationActivity>

        val ref get() = activityRef.get()
    }

    data class UIListItem(val layout: LinearLayout, val selected: TextView)

    var selectedBikeModel = 0
    var selectedMiscModel = 0

    val miscNames = arrayOf("Misc 1", "Misc 2", "Misc 3")
    lateinit var miscButtons: List<UIListItem>

    val bikeNames = arrayOf("Bike model 1", "Bike model 2", "Bike model 3")
    lateinit var bikeButtons: List<UIListItem>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        miscButtons = listOf(UIListItem(misc1Layout, misc1SelectedTextView),
                             UIListItem(misc2Layout, misc2SelectedTextView),
                             UIListItem(misc3Layout, misc3SelectedTextView))

        bikeButtons = listOf(UIListItem(bikeModel1Layout, bikeModel1SelectedTextView),
                             UIListItem(bikeModel2Layout, bikeModel2SelectedTextView),
                             UIListItem(bikeModel3Layout, bikeModel3SelectedTextView))

        Net.socket.emit(NetEvent.RequestBikeConfig.identifier)

        activityRef = WeakReference(this)
    }

    fun onBikeConfigReceived(array: JSONArray)
    {
        val bikeColor    = parseColor(array.getString(0))
        val spokeColor   = parseColor(array.getString(1))
        val miscColor    = parseColor(array.getString(4))
        val bikeModel    = array.getInt(2)
        val miscModel    = array.getInt(3)
        val miscPosition = array.getString(5) == "front"

        bikeColorButton.setColor(bikeColor)
        spokeColorButton.setColor(spokeColor)
        miscColorButton.setColor(miscColor)

        setOptionButtons(miscModel, miscNames, miscButtons, NetEvent.ChangeMiscModel, currentlySelectedMiscTextView, { selectedMiscModel = it }, { selectedMiscModel })
        setOptionButtons(bikeModel, bikeNames, bikeButtons, NetEvent.ChangeBikeModel, currentlySelectedBikeTextView, { selectedBikeModel = it }, { selectedBikeModel })

        misc_position_switch.isChecked = miscPosition
        misc_position_switch.setOnCheckedChangeListener { _, result ->
            Net.socket.emit(NetEvent.ChangeMiscPosition.identifier, if (result) "front" else "back")
        }

        configureColorPicker(bikeColorButton , NetEvent.ChangeBikeColor )
        configureColorPicker(spokeColorButton, NetEvent.ChangeSpokeColor)
        configureColorPicker(miscColorButton , NetEvent.ChangeMiscColor )

    }

    inline fun setOptionButtons(selectedModel: Int, modelNames: Array<String>, buttons: List<UIListItem>, netEvent: NetEvent, currentlySelectedTextView: TextView, crossinline setSelectedModel: (Int) -> Unit,  crossinline getSelectedModel: () -> Int)
    {
        setSelectedModel(selectedModel)

        buttons[selectedModel].selected.visibility = View.VISIBLE
        currentlySelectedTextView.text             = modelNames[selectedModel]

        var ct = 0

        for (button in buttons)
        {
            //@Note: This variable is needed because the lambda for the onClickListener would capture ct by ref instead of value leading to bugs
            val constCt = ct

            button.layout.setOnClickListener()
            {
                if (getSelectedModel() != constCt)
                {
                    for (otherButton in buttons)
                        otherButton.selected.visibility = View.INVISIBLE

                    button.selected.visibility = View.VISIBLE

                    setSelectedModel(constCt)

                    Net.socket.emit(netEvent.identifier, constCt)

                    currentlySelectedTextView.text = modelNames[constCt]
                }
            }

            ct++
        }
    }

    fun ImageButton.setColor(color: Int)
    {
        backgroundTintList = ColorStateList.valueOf(color)
    }

    fun configureColorPicker(button: ImageButton, event: NetEvent)
    {
        val builder = ColorPickerDialogBuilder.with(this@CustomizationActivity).wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(12).setNegativeButton("cancel") { _, _ -> }
                        .setTitle("Choose color")
                        .setPositiveButton("ok") { _, selectedColor, _ ->
                            button.setColor(selectedColor)
                            Net.socket.emit(event.identifier, toMuguColor(selectedColor))
                        }
                        .build()!!

        button.setOnClickListener { builder.show() }
    }
}
