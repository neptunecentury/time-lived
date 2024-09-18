A Minecraft mod that keeps track of days survived since your last death. When the player dies, a message is shown to the player indicating how long they lived and if the player surpassed the last record.
After the player respawns, a message will be sent to the player.

![Message shown to player after respawn](https://cdn.modrinth.com/data/cached_images/85dadb3445b075f5d69305f52b0a29e9a4512528.png)

**Note: This mod is still in alpha.** While the mod is tested as much as I can, it is still a good practice to back up your worlds.

Please use the Report Issues link to report issues to the github issue tracker.

See how long a player has lived
```
/timelived query [player]
```
![Time Lived command result showing how long the player has lived and the previous record](https://cdn.modrinth.com/data/cached_images/2bff36eefc8d6d0595d4c3ac20627aab4fd144c0.png)

You can customize the messages or even add new ones to the time lived messages. The default configuration settings:
```
{
  "timeLivedMessages": [
    {
      "minDaysLived": 500.0,
      "message": "Incredible! You lived for {daysLived} day(s)! You\u0027re a legend!"
    },
    {
      "minDaysLived": 366.0,
      "message": "Amazing! You lived for {daysLived} day(s). That\u0027s seriously impressive!"
    },
    {
      "minDaysLived": 365.0,
      "message": "Amazing! You lived for {daysLived} day(s). That\u0027s a whole Minecraft year!"
    },
    {
      "minDaysLived": 100.0,
      "message": "Wow! You lived for {daysLived} day(s). That is quite an accomplishment!"
    },
    {
      "minDaysLived": 1.0,
      "message": "Congrats, you lived for {daysLived} day(s)."
    },
    {
      "minDaysLived": 0.5,
      "message": "You lived for {daysLived} day(s). How about we try that again, shall we?"
    },
    {
      "minDaysLived": 0.1,
      "message": "You lived for {daysLived} day(s). Let\u0027s see if we can last a bit longer next time."
    },
    {
      "minDaysLived": 0.0,
      "message": "You lived for {daysLived} day(s). Maybe next time will be better."
    }
  ],
  "timeLivedMessagesToOthers": [
    {
      "minDaysLived": 500.0,
      "message": "Incredible! {playerName} lived for {daysLived} day(s)! Legendary!"
    },
    {
      "minDaysLived": 366.0,
      "message": "Amazing! {playerName} lived for {daysLived} day(s). That\u0027s seriously impressive!"
    },
    {
      "minDaysLived": 365.0,
      "message": "Amazing! {playerName} lived for {daysLived} day(s). That\u0027s a whole Minecraft year!"
    },
    {
      "minDaysLived": 100.0,
      "message": "Wow! {playerName} lived for {daysLived} day(s). That is quite an accomplishment!"
    },
    {
      "minDaysLived": 1.0,
      "message": "{playerName} lived for {daysLived} day(s)."
    },
    {
      "minDaysLived": 0.0,
      "message": "{playerName} only lived for {daysLived} day(s). Let\u0027s give them some encouragement!"
    }
  ],
  "newRecordMessage": "All right! New record! You surpassed your previous record of {previousDaysLived} day(s)!",
  "newRecordMessageToOthers": "All right! {playerName} surpassed their previous record of {previousDaysLived} day(s)!",
  "queryPlayerMessage": "{playerName} has lived for {daysLived} day(s). Previous record is {previousDaysLived} day(s).",
  "statsNotFoundMessage": "Statistics not found for {playerName}.",
  "timeTravelMessage": "Wait... did you travel back in time?",
  "enableMessagesToOthers": true
}
```

You can use variables in the customer messages to insert the number of days the player lived, their previous record, and also the player name.
```
{daysLived}
{previousDaysLived}
{playerName}
```

To add a new message to the time lived messages, add a new section like this:
```
{
  "minDaysLived": 1000.0,
  "message": "Holy moly! You lived for {daysLived} day(s)! You\u0027re a superstar!"
}
```

Now if you live for 1000 days or more, you will get the new message.
