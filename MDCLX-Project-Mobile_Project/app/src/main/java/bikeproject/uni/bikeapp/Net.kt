@file:Suppress("NOTHING_TO_INLINE")

package bikeproject.uni.bikeapp

import android.app.Activity
import android.os.Handler
import android.os.Looper
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import java.lang.ref.WeakReference

// @Note: that these are just placeholders
enum class NetEvent(val identifier: String)
{
    CreateAccount("accountRegister"),
    CreateAccountSuccess("userRegisterSuccess"),
    EmailAlreadyRegistered("usernameTaken"),
    GamerTagAlreadyRegistered("gamertagTaken"),

    Login("login"),

    LoginFailure("loginFailure"),
    LoginSuccess("loginSuccess"),
    ManagerConnectionSuccess("managerLoginSuccess"),
    UserNotRegistered("userNotRegistered"),

    ConnectToVr("connectToVrDevice"),

    ChangeBikeColor   ("serverChangeBikeColour"  ),
    ChangeSpokeColor  ("serverChangeSpokeColour" ),
    ChangeBikeModel   ("serverChangeBikeModel"   ),
    ChangeMiscModel   ("serverChangeMiscModel"   ),
    ChangeMiscColor   ("serverChangeMiscColour"  ),
    ChangeMiscPosition("serverChangeMiscPosition"),

    GetLeaderBoardResults("mobileLeaderboardResults"),
    OnLeaderBoardResults("sentMobileLeaderboard"),

    RequestBikeConfig("modifierGetBikeConfig"),
    GetBikeConfig("modifierBikeConfigReturn"),

    RequestLeaderboardPosition("getLeaderboardPosition"),
    GetLeaderboardPosition("sentCurrentLeaderboard"),

    GetManagerInfo("getManagerInfo"),
    OnManagerInfoReceived("sentManagerInfo"),

    OnVrDisconnect("vrDisconnect"),
}

inline fun Emitter.register(event: String,   crossinline proc: (Array<Any>) -> Unit): Emitter = this.on(event)            { Handler(Looper.getMainLooper()).post { proc(it) } }
inline fun Emitter.register(event: NetEvent, crossinline proc: (Array<Any>) -> Unit): Emitter = this.on(event.identifier) { Handler(Looper.getMainLooper()).post { proc(it) } }

object Net
{
    var CurrentUserName = ""

    // @Todo: Specify the server address
    const val ServerAddress = "http://10.41.179.170:3000/"

    lateinit var socket: Socket
    lateinit var currentActivityRef: WeakReference<Activity>

    val currentActivity: Activity? get() = currentActivityRef.get()

    fun connect(ip: String = ServerAddress)
    {
        // @Todo: Add all the events once the server is ready and the API si decided upon
        socket = IO.socket("http://$ip:3000/") dot
        {
            register(Socket.EVENT_CONNECT)       { socket.emit("register", false)                }
            register(Socket.EVENT_CONNECT_ERROR) { println("Connection error: ${it.toString()}") }
            register(Socket.EVENT_DISCONNECT)    { MainActivity.ref?.onVrDisconnect()            }

            register(NetEvent.EmailAlreadyRegistered)    { CreateAccountActivity.ref?.onCreateAccountResult(true, false)  }
            register(NetEvent.GamerTagAlreadyRegistered) { CreateAccountActivity.ref?.onCreateAccountResult(false, true)  }
            register(NetEvent.CreateAccountSuccess)      { CreateAccountActivity.ref?.onCreateAccountResult(false, false) }

            register(NetEvent.LoginFailure)             { LoginActivity.ref?.onLoginResult(success= false, unknownUser= false, isManager= false) }
            register(NetEvent.LoginSuccess)             { LoginActivity.ref?.onLoginResult(success= true , unknownUser= false, isManager= false) }
            register(NetEvent.ManagerConnectionSuccess) { LoginActivity.ref?.onLoginResult(success= true , unknownUser= false, isManager= true)  }
            register(NetEvent.UserNotRegistered)        { LoginActivity.ref?.onLoginResult(success= false, unknownUser= true , isManager= false) }

            register(NetEvent.GetLeaderboardPosition)
            {
                MainActivity.ref?.onGetLeaderboardPosition((it[0] as JSONArray).getInt(0))
            }

            register(NetEvent.GetBikeConfig)
            {
                val array = it[0] as JSONArray

                CustomizationActivity.ref?.onBikeConfigReceived(array)
            }

            register(NetEvent.OnLeaderBoardResults)
            {
                val array = it[0] as JSONArray
                val list = ArrayList<String>()

                for (i in 0 until array.length())
                {
                    val str = array[i].toString()
                    val strSplit = str.split(" ")
                    val name = strSplit[0]
                    val score = strSplit[1]

                    list.add("$name with score: $score")
                }

                LeaderBoardActivity.ref?.onLeaderBoardResults(list)
            }

            register(NetEvent.OnManagerInfoReceived)
            {
                ManagerActivity.ref?.onManagerInfoReceived(it)
            }

            register(NetEvent.OnVrDisconnect)
            {
                MainActivity.ref?.onVrDisconnect()
            }
        }

        socket.connect()
    }
}