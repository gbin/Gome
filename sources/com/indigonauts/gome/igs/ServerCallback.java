/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.igs;

public interface ServerCallback {
    
    static final byte MESSAGE_ERROR_TYPE = 0x0;
    static final byte MESSAGE_CHAT_TYPE = 0x1;
    static final byte MESSAGE_KIBITZING_TYPE = 0x2;
    
    void loggedEvent();

    void gameListEvent(ServerGame[] games);

    void userListEvent();

    void observeEvent(ServerMove[] moves);

    void moveEvent(ServerMove move);

    void challenge(ServerChallenge challenge);

    void message(byte type, String nick, String message);

    void startGame(ServerChallenge challenge);

    void synOnlineTime(int whiteTime, int whiteByoStone, int blackTime, int blackByoStone);

    void timesUP(String name);

    void endGame();

    void restoreGameForCounting();

    void oppRemoveDeadStone(byte x, byte y);

    void gameIsDone(String player1, int score1, String player2, int score2);

    void oppSetHandicap(byte handicap);

    void oppWantToSetNewKomi(byte komi);

    void setKomi(byte komi);

    void onlineSetKomi(byte komi);

    void onlineResigned(String name);
    
    void onlineScore(int whiteScore,int blackScore);
    
    void winByValue(String name, int winByValue);
}
