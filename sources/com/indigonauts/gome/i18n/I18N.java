package com.indigonauts.gome.i18n;

//#if LOCALE=="en_US" 
public interface I18N {
  public interface clock {
    String blackTimeUp = "Black's time is up. White won.";
    String min25stones = "min/25 stones";
    String timesUp = "'s time is up";
    String whiteTimeUp = "White's time is up. Black won.";
  }

  public interface count {
    String doneWithScoring = "Done with scoring. Waiting for opponent to finish.";
    String endGameMarkStone = "The game ended, please mark dead stones";
    String markDeadStone = "Mark dead stones";
    String restoreCounting = "The game is restored for re-scoring";
    String undoMarkDeadStone = "Reset dead stones";
    String evaluate = "Score Est.";
    String endCounting = "End Scoring";

  }

  public interface game {
    String application = "Application :";
    String blackLong = "Black";
    String blackShort = "(B)";
    String blackWin = "B+";
    String captured = "Captured :";
    String context = "Context :";
    String copyright = "Copyright :";
    String event = "Event :";
    String info = "Game info";
    String jigo = "Jigo";
    String komi = "Komi :";
    String name = "Famous name :";
    String opening = "Opening :";
    String passed = "passed.";
    String place = "Place :";
    String result = "Result :";
    String round = "Round :";
    String score = "score :";
    String scribe = "Scribe :";
    String source = "Source :";
    String versus = "vs";
    String whiteLong = "White";
    String whiteShort = "(W)";
    String whiteWin = "W+";
    String winBy = "wins by";
  }

  public interface bt {
    String connect = "Connect to Bluetooth";
    String connecting = "Looking for remote Gome";
    String connectionError = "Bluetooth connection error";
    String noPeerFound = "Error : No other Gome found.";
    String peerList = "Available remote gomes";
    String connected = "Connected to Bluetooth Peer";
    String disconnect = "Disconnect Bluetooth";
  }

  public interface online {
    String acceptChallenge = "Accepting Challenge...";
    String blackGives = "Black Gives";
    String challenge = "Challenge";
    String challengeMessage = "Your color : %0 |Goban size : %1 x %2 |Time : %3 min %4";
    String challengesYou = "challenges you";
    String changeHandicap = "Change Handicap";
    String connect = "Connect to IGS";
    String connectedToIgs = "You are now connected to IGS";
    String connecting = "Connecting...";
    String connectionError = "Connection Error";
    String decline = "Decline";
    String disconnect = "IGS Disconnect";
    String disconnected = "Disconnected";
    String gameList = "Online Gamelist";
    String getUserList = "Getting User List...";
    String gettingGameList = "Getting Game List...";
    String gettingScore = "Getting final score...";
    String handicapChangeForm = "Change Handicap";
    String komiChangeForm = "Change Komi";
    String loginError = "Invalid login or password.";
    String message = "Send Message";
    String messageSent = "Message sent";
    String movePlayed = "A new move has been played";
    String newKomi = "komi has been set to ";
    String noByo = "No byoyomi";
    String noStartedGame = "No game has been started";
    String notYourTurn = "Not your turn";
    String observe = "Observe";
    String onlineGameStarted = "Online game started";
    String opponentAgreedNewKomi = "Your opponent has agreed to change the komi";
    String opponentWantsToChangeKomi = "Your opponent want to change the komi to";
    String requestKomi = "Request Komi Change";
    String requestingBlackKomi = "Requesting that Black gives a komi of";
    String requestingWhiteKomi = "Requesting that White gives a komi of";
    String sendChallenge = "Sending Challenge...";
    String userlist = "Online Users";
    String versionError = "The server version doesn't match the one expected by Gome. Please Upgrade Gome.";
    String whiteGives = "White Gives";
    String youWantToChangeHandicap = "You want to set the handicap to";
    String youWantToChangeKomi = "You want to change the komi to";
    String sortRank = "Sort by Rank";
    String sortNick = "Sort by Nick";
    String sortWatch = "Sort by Watch";
  }

  public interface help {
    String help = "Help";
    String comment = "expand comments";
    String hint = "show next possible moves";
    String next10Moves = "jump 10 moves";
    String pointer = "Use arrows to move the pointer";
    String pointerReview1 = "RIGHT Next Move";
    String pointerReview2 = "LEFT Prev Move";
    String pointerReview3 = "UP Prev Variation";
    String pointerReview4 = "DOWN Next Variation";
    String prev10Moves = "jump back 10 moves";
    String scrollDown = "scroll down comment";
    String scrollUp = "scroll up comment";
    String undo = "undo";
    String zoom = "zoom around the pointer";
    String rules = "Go rules (online)";
    String gome = "Gome";
    String key = "Shortcuts";
    String prevCorner = "Previous Corner";
    String nextCorner = "Next Corner";

  }

  public interface option {
    String fast = "Fast";
    String gobanColor = "Goban";
    String igs = "IGS Options";
    String igsByoyomi = "Min for 25 moves";
    String igsChallenge = "- Challenge -";
    String igsSize = "Goban";
    String manual = "Manual scroll";
    String medium = "Medium";
    String oneHalf = "One & half liner";
    String oneLiner = "One liner";
    String scrollerFont = "Scroller Font";
    String scrollerSize = "Scroller Size";
    String scrollerSpeed = "Scroller speed";
    String slow = "Slow";
    String twoHalf = "Two & half liner";
    String twoLiner = "Two liner";
    String register = "Register";
    String user = "Name";
    String key = "Key";
    String invalidKey = "Registration key invalid";
    String invalidKeyExplanation = "The entered registration information is invalid, please check if your name and key are correct.";
    String light = "light";
    String dark = "dark";
    String small = "small";
    String large = "large";
    String igsMinutes = "Main Time";
    String aspect = "Graphics";
    String stoneBug = "Select the best looking stone";
    String stone = "Stone";
    String optimize = "Optimize for ...";
    String speed = "speed and battery";
    String memory = "memory";
    String compatibility = "Compatibility";
    String ghostStone = "Ghost Stone";
    String email = "Your Email";
    String bluetooth = "Bluetooth Service";
    String start = "Start";
    String dontstart = "Don't start";
  }

  public interface error {
    String error = "Error";
    String errorDelete = "Error while deleting file";
    String onlyOnline = "You can only import online files";
    String recordStored = "Error accessing internal storage";
    String sgfParsing = "SGF format error";
    String stream = "Closed stream";
    String wrongtype = "Wrong file type";
    String onlyLocal = "Only local files can be deleted";
    String posting = "Unable to post file to server";
  }

  String about = "About";
  String accept = "Accept";
  String back = "Back";
  String blackResigned = "Black has resigned";
  String changedLanguage = "You need to restart the application to apply your language change";
  String dark = "Dark";
  String defaultFilename = "game";
  String delete = "Delete";
  String done = "Done";
  String download_failure = "Download failure.";
  String download_inprogress = "Loading ...";
  String easterEgg = "Good luck !";

  String exit = "Exit";
  String failure = "Failure";
  String filename = "Filename";
  String firstMove = "Go to first move";
  String gameHadEnded = "This game has ended. Please start a new game";
  String gameStatus = "Game Info";
  String goban = "Goban size...";
  String handicap = "Handicap";

  String import_ = "Import";
  String info = "Info";
  String komi = "Komi";
  String large = "Large";
  String lastMove = "Go to last move";
  String library = "Library";
  String light = "Light";
  String medium = "Medium";
  String menu = "Menu";
  String needReboot = "Restart";
  String new_ = "New";
  String nextInCollection = "Next";
  String noMoreMove = "No more moves";
  String nogame = "No Game found to resume";
  String nohandicap = "Scratch";
  String notValidMove = "Invalid move";
  String open = "Open";
  String login = "Login";
  String password = "Password";
  String options = "Options";
  String pass = "Pass";
  String playMode = "Play Mode";
  String random = "Random";
  String request = "Request";
  String resign = "Resign";
  String resigned = "resigned.";
  String resume = "Resume";
  String reviewMode = "Review Mode";
  String selectall = "Select All";
  String settingHandicapTo = "Setting Handicap to";
  String small = "Small";
  String start = "Start";
  String switchToPlayEditMode = "Play/edit mode";
  String switchToReviewMode = "Review mode";
  String whiteResigned = "White has resigned.";
  String wrongMove = "Incorrect";
  String rightMove = "Correct !";
  String success = "Success";
  String email_success = "Your game has been successfully send.";
  String sendByEmail = "Send by Email";
  String expired = "Your trial period has expired. ";
  String expiredExplanation = "You enjoyed Gome ? Buy it on http://www.indigonauts.com/gome/ !";
  String hoursLeft = "You have %0 hours %1 minutes left on your trial period";
  String send = "Send";
  String loginProblem = "Error during the login";
  String noBundle = "This item is not installed in this version of Gome";
  String fileselect = "Files";
  String filesIn = "In %0";
  String saveIn = "Save in %0";
  String save = "Save";
  String comment = "Comments";
  String zoom = "Zoom";
  String undo = "Undo";
  String hint = "Hint";
  String openReview = "Open for review";
  String previousInCollection = "Previous";
  String next10Moves = "Jump 10 moves";
  String prev10Moves = "Back 10 moves";
  String saveAs = "Save as...";
  String reply = "reply";
  String editNode = "Edit";
  String ok = "ok";
  String bookmark = "Set bookmark";
  String gotoBookmark = "Go bookmark";
  String bookmarkSet = "The bookmark has been set";
}
//#elif LOCALE=="fr_FR"
//#include fr.incl
//#endinclude
//#endif