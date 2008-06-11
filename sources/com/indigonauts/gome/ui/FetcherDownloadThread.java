/*
 * (c) 2005 Indigonauts
 */
package com.indigonauts.gome.ui;

class FetcherDownloadThread extends Thread {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("FetcherDownloadThread");
  //#endif    
  private final Fetcher fetcher;

  /**
   * @param fetcher
   */
  FetcherDownloadThread(Fetcher fetcher) {
    this.fetcher = fetcher;
    log.debug("--> Fetcher Created");
  }

  public void run() {
    log.debug("--> Fetcher Started");
    this.fetcher.status = Fetcher.WORKING;
    try {
      this.fetcher.download();
    } catch (Exception e) {
      e.printStackTrace();
      this.fetcher.status = Fetcher.FAIL;
      this.fetcher.downloadFailed(e);
      return;
    }

    if (this.fetcher.status == Fetcher.TERMINATED)
      return;

    this.fetcher.status = Fetcher.SUCCESS;
    this.fetcher.downloadFinished();
    log.debug("--> Fetcher Ended");
  }
  
}