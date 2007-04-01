/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.io.IndexEntry;
import com.indigonauts.gome.io.URLFileEntry;

public class FileImporter extends Fetcher {
    private FileBrowser callback;
    private FileEntry selectedFile;

    public FileImporter(FileBrowser callback, FileEntry selectedFile) {
        super(selectedFile.getPath());
        this.callback = callback;
        this.selectedFile = selectedFile;
    }

    protected void download() throws IOException {
        if (selectedFile instanceof IndexEntry) {
            Vector fileList = IOManager.singleton.getFileList(selectedFile.getPath(), this);
            Enumeration allFiles = fileList.elements();
            while (allFiles.hasMoreElements() && status != TERMINATED) {
                Object toDownload = allFiles.nextElement();
                if (toDownload instanceof URLFileEntry) {
                    downloadFile(((URLFileEntry) toDownload).getPath());
                }
            }
        } else
            downloadFile(selectedFile.getPath());
    }

    /**
     * @throws IOException
     */
    private void downloadFile(String fileUrl) throws IOException {
        byte[] file = IOManager.singleton.loadFile(fileUrl, this);
        String name = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        gauge.setLabel(name);
        try {
            IOManager.singleton.saveLocalStore(name, file);
        } catch (RecordStoreException e) {
            throw new IOException("ui.error.recordStore"); //$NON-NLS-1$
        }
    }

    protected void downloadFinished() {
        callback.done();
    }

    protected void downloadFailed(Exception reason) {
        callback.failed(reason);
    }

}
