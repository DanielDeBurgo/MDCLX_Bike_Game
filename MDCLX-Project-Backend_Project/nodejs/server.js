//modifiers means mobileDevices

//required libraries for database, hasing and network IO
const bcrypt = require('bcrypt')
const saltRounds = 10
const mongoose = require('mongoose')

mongoose.connect("mongodb://localhost:27017/serverdb", { useNewUrlParser : true})

const userdb = require("./models/user.js")
const leaderboarddb = require("./models/leaderboard.js")

var app = require("express")()
var server = require("http").Server(app)
var io = require("socket.io")(server)

var managers = []

//global variables
var port = 3000
var modifiers = { }
var vrDevices = { }

var socketMap = {}


var debug = 0
//objects to represent phones and VR vrDevices
function Modifier(id){
    this.id = id
    this.vrDeviceId = 0

    this.getSocket = function() { return io.sockets.connected[this.id] }
    this.isPairedWithDevice = function() { return this.vrDeviceId != 0 }
    this.getVrDevice = function() { return vrDevices[this.vrDeviceId] }
}


function VRDevice(id){
    this.id = id
    this.modifierId = 0

    this.getSocket = function() { return io.sockets.connected[this.id] }
    this.isPairedWithDevice = function() { return this.modifierId != 0 }
    this.getModifier = function() { return modifiers[this.modifierId] }
}

//start the server
server.listen(port, function() {
  console.log("Listening on port " + port + "\n");
});

//utility functions
function copy(object) { return JSON.parse(JSON.stringify(object)) }

function forEachIn(iterable, action){
    for (var key in iterable){
        if (iterable.hasOwnProperty(key)){
            action(iterable[key])
        }
    }
}

function forEachInArray(array, action){
    for (var i = 0; i < array.length; i++){
        action(array[i])
    }
}

function asArray(iterable){
    var array = []
    forEachIn(iterable, function(obj) { array.push(obj) })
    return obj
}

var testingVRDeviceId = 0


//Handle the connection event
io.on("connection", function(socket)
{
    console.log("[connection] Device id: " + socket.id + " connected!\n")
    // socket.emit("test")
    // socket.on("test2", function(theMessage){
    //   console.log(theMessage)
    // })

    //Handle the register event
    socket.on("register", function(isVR){
        if (isVR){
            var vrDevice = new VRDevice(socket.id)
            vrDevices[socket.id] = vrDevice
            //testingVRDeviceId = socket.id
            console.log("[register] VR registered: " + socket.id + "\n")

            //emits QR code
            socket.emit("registerSuccess", "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + socket.id)
        }
        else{
            var modifier = new Modifier(socket.id)
            modifiers[socket.id] = modifier

            console.log("[register] Modifier registered: " + socket.id + "\n")

            socket.emit("registerSuccess")
        }
    })


    // This function is used to connect to a vr device.
    socket.on("connectToVrDevice", function(id){
        console.log("[connectToVrDevice] Modifier id: " + socket.id + " VR Device id:" + id + "\n")

        if (id in vrDevices){
            var vrDevice = vrDevices[id]

            vrDevice.modifierId = socket.id
            socket.emit("connectToVrDeviceSuccess")
            vrDevice.getSocket().emit("connectedToDevice")

            modifiers[socket.id].vrDeviceId = vrDevice.id

            //set daily challenge
            var today = new Date()
            var day = String(today.getDate()).padStart(2,'0')
            var challenge = parseInt(day,10) % 2

            if (challenge == 0){
              vrDevice.getSocket().emit("setDailyChallenge", "uber-eats")
            }
            else{
              vrDevice.getSocket().emit("setDailyChallenge", "checkpoints")
            }

            return
        }

        console.log("[error:connectToVrDevice] VR Device" + id + "not found")
    })


    //Handle disconnect event
    socket.on("disconnect", function(){
        //Remove device from any data structures it is in and tell connected devices it is gone
        if (socket.id in vrDevices){
            if (vrDevices[socket.id].modifierId != 0){
                var modifier = vrDevices[socket.id].getModifier()

                modifier.vrDeviceId = 0
                modifier.getSocket().emit("vrDeviceDisconnected")

                console.log("[disconnect] VR Device id: " + socket.id + " modifier id: " + modifier.id + "\n")
            }
            else
            {
                console.log("[disconnect] VR Device id: " + socket.id + " without modifier \n")
            }

            delete vrDevices[socket.id]
        }
        else if (socket.id in modifiers){
            if (modifiers[socket.id].vrDeviceId != 0){
                var vrDevice = vrDevices[modifiers[socket.id].vrDeviceId]

                vrDevice.modifierId = 0
                vrDevice.getSocket().emit("modifierDisconnected")

                console.log("[disconnect] Modifier id: " + socket.id + " VR device id:" + vrDevice.getSocket().id + "\n")
            }
            else{
                console.log("[disconnect] Modifier id: " + socket.id + " without VR device \n")
            }

            delete modifiers[socket.id]
        }
        else{
            console.log("[disconnect] Device id: " + socket.id + "\n")
        }
    })



    if (debug == 0)
    {
      //Hand registation of new user
      socket.on("accountRegister", function(sentUserName, sentPassword, sentGamertag){
        var countDocDidNotWork = false
        //Try finding the gamertag in case it is taken
        userdb.collection.find({gamertag : sentGamertag}).count(function(err,res){
          if (res != 0){
            socket.emit("gamertagTaken")
            countDocDidNotWork = true
          }
          //If the gamertag is not taken
          if (countDocDidNotWork == false){
            //Check is username is taken
            userdb.collection.find({userName : sentUserName}).count(function(err,res){
              if (res != 0){
                socket.emit("usernameTaken")
                countDocDidNotWork = true
              }
              //So long as it isn't taken as well add them into the database and let them know they're successfully registered
              if (countDocDidNotWork == false){
                bcrypt.hash(sentPassword,saltRounds,function(err,hash){
                  userdb.collection.insertOne({userName : sentUserName, password : hash ,gamertag : sentGamertag, manager : false, time : 1000, score : 0, highScore: 0, highScoreTimeStamp: Date(Date.now()).toString(), gamesPlayed : 1, bikeColour : "(R=1,G=1,B=1,A=1)", spokeColour : "(R=1,G=1,B=1,A=1)", bikeModel : 1, miscModel : 1, miscColour  : "(R=1,G=1,B=1,A=1)", miscPosition : "inFront"})
                  socket.emit("userRegisterSuccess")
                })
              }
            })
          }
        })
      })


      //Handles a login attempt
      socket.on("login", function(sentUserName, sentPassword){
        var found = false
        //Check their username exists
        userdb.collection.find({userName : sentUserName}).count(function(err,res){
          if (res != 0){
            //If it does check the hashed password for that user is same as sent hashed password
            userdb.collection.findOne({userName : sentUserName}, function(err, user){
              bcrypt.compare(sentPassword, user.password, function(err, res){
                if (res){
                  //Handle manager login
                  if (user.manager){
                    socket.emit("managerLoginSuccess")
                    socketMap[socket.id] = sentUserName
                  }
                  else{
                    socket.emit("loginSuccess")
                    socketMap[socket.id] = sentUserName
                  }
                }
                else{
                  socket.emit("loginFailure")
                }
              })
            })
          }
          else{
            socket.emit("incorrectUserName")
          }
          found = true
        })

        //If the username was not found they might be trying to log in with gamertag so we try this and handle same way as above
        if (!found){
          userdb.collection.find({gamertag : sentUserName}).count(function(err,res){
            if (res != 0){
              userdb.collection.findOne({gamertag : sentUserName}, function(err, user){
                bcrypt.compare(sentPassword, user.password, function(err, res){
                  if (res){
                    if (user.manager){
                      socket.emit("managerLoginSuccess")
                      socketMap[socket.id] = sentUserName
                    }
                    else{
                      socket.emit("loginSuccess")
                      socketMap[socket.id] = sentUserName
                    }
                  }
                  else{
                    socket.emit("loginFailure")
                  }
                })
              })
            }
            else{
              socket.emit("incorrectUserName")
            }
            found = true
          })
        }

        if (!found){
          socket.emit("userNotRegistered")
        }
      })


      //Gets the current configuration of the bike and returns it to a VR device
      socket.on("getBikeConfig", function(){
        var userName = socketMap[vrDevices[socket.id].getModifier().getSocket().id]
        userdb.collection.findOne({userName : userName}, function(err,res){
          socket.emit("changeBikeColour", res.bikeColour)
          socket.emit("changeSpokeColour", res.spokeColour)
          socket.emit("changeBikeModel", res.bikeModel)
          socket.emit("changeMiscModel", res.miscModel)
          socket.emit("changeMiscColour", res.miscColour)
          socket.emit("changeMiscPosition", res.miscPosition)
          // socket.emit("bikeConfigReturn", res.bikeColour, res.spokeColour, res.bikeModel, res.miscModel, res.miscColour, res.miscPosition)
        })
      })

      //Gets current configuration of the bike and returns it to a mobile device
      socket.on("modifierGetBikeConfig",function(){
        var userName = socketMap[socket.id]
        userdb.collection.findOne({userName : userName}, function(err,res){
          socket.emit("modifierBikeConfigReturn", [res.bikeColour, res.spokeColour, res.bikeModel, res.miscModel, res.miscColour, res.miscPosition])
        })
      })

      //Managers should only be able to emit this from mobile devices. Gives raw data to view as stats
      socket.on("getManagerInfo", function(){
        //Number of games played, array of usernames,
        arrUserNames = []
        arrHighPoints = []
        arrHighTimes = []
        arrHighTimeStamps = []
        arrGamesPlayed = []
        userdb.collection.find().toArray(function(err, res){
          res.forEach(function(currentUser){
            arrUserNames.push(currentUser.userName)
            arrHighPoints.push(currentUser.highScore)
            arrHighTimeStamps.push(currentUser.highScoreTimeStamp)
            arrHighTimes.push(currentUser.time)
            arrGamesPlayed.push(currentUser.gamesPlayed)
          })
          socket.emit("sentManagerInfo", arrUserNames, arrHighPoints, arrHighTimes, arrHighTimeStamps, arrGamesPlayed)
        })
      })

      //What follows is a series of events to change the configuration from a mobile device, save to a database and let the VR device know to change the model in some way
      socket.on("serverChangeBikeColour", function(colour) {
        userdb.collection.updateOne(
          {userName : socketMap[socket.id]},
          {$set:{bikeColour : colour}}
        )
        modifiers[socket.id].getVrDevice().getSocket().emit("changeBikeColour", colour)
      })


      socket.on("serverChangeSpokeColour", function(colour) {
        userdb.collection.updateOne(
          {userName : socketMap[socket.id]},
          {$set:{spokeColour : colour}}
        )
        modifiers[socket.id].getVrDevice().getSocket().emit("changeSpokeColour", colour)
      })


      socket.on("serverChangeBikeModel", function(modelID) {
        userdb.collection.updateOne(
          {userName : socketMap[socket.id]},
          {$set:{bikeModel : modelID}}
        )
        modifiers[socket.id].getVrDevice().getSocket().emit("changeBikeModel", modelID)
      })


      socket.on("serverChangeMiscModel", function(modelID) {
        userdb.collection.updateOne(
          {userName : socketMap[socket.id]},
          {$set:{miscModel : modelID}}
        )
        modifiers[socket.id].getVrDevice().getSocket().emit("changeMiscModel", modelID)
      })


      socket.on("serverChangeMiscColour", function(colour) {
        userdb.collection.updateOne(
          {userName : socketMap[socket.id]},
          {$set:{miscColour : colour}}
        )
        modifiers[socket.id].getVrDevice().getSocket().emit("changeMiscColour", colour)
      })


      socket.on("serverChangeMiscPosition", function(inFront) {
        userdb.collection.updateOne(
          {userName : socketMap[socket.id]},
          {$set:{miscPosition : inFront}}
        )
        modifiers[socket.id].getVrDevice().getSocket().emit("changeMiscPosition", inFront)
      })

      socket.on("mobileLeaderboardResults",function(){
        var sendLeaderboard = []
        userdb.collection.find().sort({highScore:-1}).toArray(function(err,res){
          res.forEach(function(currentUser){
            sendLucas.push(currentUser.userName + " " + currentUser.highScore)
          })
          socket.emit("sentMobileLeaderboard", sendLeaderboard)
        })
      })

      //Called when the VR device finished a game, we update the leaderboard as required in the database
      socket.on("sendResults", function(arrResults){
        var sentTime = arrResults[0]
        var sentPoints = arrResults[1]
        userdb.collection.updateOne(
          {userName : socketMap[vrDevices[socket.id].getModifier().getSocket().id]},
          {$set:{time : sentTime}}
        )
        userdb.collection.updateOne(
          {userName : socketMap[vrDevices[socket.id].getModifier().getSocket().id]},
          {$set:{score : sentPoints}}
        )

        userdb.collection.findOne({userName:socketMap[vrDevices[socket.id].getModifier().getSocket().id]}, function(err, res){
          if (res.highScore < sentPoints){
            userdb.collection.updateOne(
              {userName : res.userName},
              {$set:{highScore : sentPoints}}
            )
            userdb.collection.updateOne(
              {userName : res.userName},
              {$set:{highScoreTimeStamp : Date(Date.now()).toString()}}
            )
          }
          userdb.collection.updateOne(
            {userName : socketMap[vrDevices[socket.id].getModifier().getSocket().id]},
            {$set:{gamesPlayed : res.gamesPlayed + 1}}
          )
        })
      })

      //Called when the VR device wants to know how the leaderboard looks and where their user is, returns exactly this
      socket.on("serverSendCurrentLeaderboard", function(){
        var sendToVrResults = ""
        userdb.collection.find().sort({highScore:-1}).limit(4).toArray(function(err,res){
          res.forEach(function(currentUser){
            sendToVrResults=sendToVrResults + currentUser.userName + "," + currentUser.score + "," + currentUser.time + "\n"
          })
          var count = 1
          userdb.collection.find().sort({highScore:-1}).forEach(function(currentUser){
            if (currentUser.userName == socketMap[vrDevices[socket.id].getModifier().getSocket().id]){
              sendToVrResults=sendToVrResults + currentUser.userName + "," + currentUser.score+ "," + currentUser.time + "," + count + "\n"
              socket.emit("serverSentCurrentLeaderboard",sendToVrResults)
            }
            count++
          })
        })
      })

      //Called when mobile needs leaderboard position of the current user
      socket.on("getLeaderboardPosition",function(){
        var count = 1
        userdb.collection.find().sort({highScore:-1}).forEach(function(currentUser){
          if (currentUser.userName == socketMap[socket.id]){
            socket.emit("sentCurrentLeaderboard",[count])
          }
          count++
        })
      })
    }
});
