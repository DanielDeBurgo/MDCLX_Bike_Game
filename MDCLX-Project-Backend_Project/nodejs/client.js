const io = require("socket.io-client")


var socketIO = io.connect('http://10.41.179.170:3000/')//io.connect('http://178.62.105.148:3000/')



socketIO.emit('register', false)

socketIO.emit('accountRegister', "test","test2","testing")
socketIO.on("userRegisterSuccess",function(){
  console.log("registration succcessful")
  socketIO.emit('login',"test","testFail")
  socketIO.on('loginSuccess',function(){
    console.log("login Success")
  })
  socketIO.on('loginFailure',function(){
    console.log("login failed")
  })
})

// socketIO.on('registerSuccess',function(){
//   console.log("modifierRegistered\n")
//   socketIO.emit("connectToVrDevice",'5NVv1S6Acp_LhJCQAAAC')
//   socketIO.on("connectToVrDeviceSuccess",function(){
//     console.log("connectionToVrSuccessful")
//
//     socketIO.emit('serverChangeBikeColour', "(R=1,G=0.549,B=0,A=1)")
//     console.log("emitted change bike colour")
//
//     socketIO.emit('serverChangeSpokeColour', "(R=1,G=0.549,B=0,A=1)")
//     console.log("emitted change spoke colour")
//
//     socketIO.emit('serverChangeBikeModel', "1")
//     console.log("emitted change bike model")
//
//     socketIO.emit('serverChangeMiscModel', "2")
//     console.log("emitted change misc model")
//
//     socketIO.emit('serverChangeMiscColour', "(R=1,G=0.549,B=0,A=1)")
//     console.log("emitted change misc colour")
//
//     socketIO.emit('serverChangeMiscPosition', "Back")
//     console.log("emitted change misc position")
//   })
//
// })
