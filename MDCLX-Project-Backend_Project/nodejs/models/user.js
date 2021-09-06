var mongoose = require('mongoose');

var credentialSchema = mongoose.Schema({
  userName : String,
  password : String,
  gamertag : String,
  manager : Boolean,

  time : Number,
  score : Number,
  highScore : Number,
  highScoreTimeStamp : String,
  gamesPlayed : Number,

  bikeColour : String,
  spokeColour : String,
  bikeModel : Number,
  miscModel : Number,
  miscColour : String,
  miscPosition : String
});


module.exports = mongoose.model('user', credentialSchema);
