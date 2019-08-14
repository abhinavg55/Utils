/*
 * ====================================================================
 * Copyright (c) 2004-2008 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package com.abhinav.svn;

import java.io.File;
import java.time.LocalDateTime;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class WorkingCopy {

    private static SVNClientManager ourClientManager;
    private static ISVNEventHandler myUpdateEventHandler;
    private static ISVNEventHandler myWCEventHandler;
    static int WAKE_TIME_MINS = 60;
    public static void main(final String[] args) throws SVNException, InterruptedException {
    	
        final String gmailUserName = args[0];
        final String gmailPassword = args[1];
        String wakeTimeMins = args[2];
        String paths = args[3];
        final String[] pathsArray = paths.split(",");
    	final Integer wakeTimeMinsInt = wakeTimeMins != null ? Integer.parseInt(wakeTimeMins) : WAKE_TIME_MINS;
    	Runnable r = new Runnable() {
			public void run() {
				while(true) {
					execute(gmailUserName, gmailPassword, pathsArray);
					System.out.println("Will try after " + wakeTimeMinsInt + " minutes.");
					System.out.println("-------------");
					try {
						Thread.sleep(wakeTimeMinsInt * 1000 * 60);
					} catch (InterruptedException e) {
					}
				}
			}
		};
    	new Thread(r).start();
    	
    }

	private static void execute(String userName, String password, String[] pathsArray) {
		for (String path : pathsArray) {
	        doUpdate(path, userName, password);
	        System.out.println(">>>>>>>>>>>>>>>>>>");
		}
	}

	private static void doUpdate(String path, String gmailUserName, String gmailPassword) {
		setupLibrary();
		System.out.println("Current time is: " + LocalDateTime.now());

        String myWorkingCopyPath = path;
        myUpdateEventHandler = new UpdateEventHandler(path, gmailUserName, gmailPassword);
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ourClientManager = SVNClientManager.newInstance(options);
        ourClientManager.getUpdateClient().setEventHandler(myUpdateEventHandler);
        ourClientManager.getWCClient().setEventHandler(myWCEventHandler);
        File wcDir = new File(myWorkingCopyPath);
        System.out.println("Updating '" + wcDir.getAbsolutePath() + "'...");
        try {
            update(wcDir, SVNRevision.HEAD, true);
        } catch (SVNException svne) {
            error("error while recursively updating the working copy at '"
                    + wcDir.getAbsolutePath() + "'", svne);
        }
	}
    
    private static void setupLibrary() {
 
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }
    
    private static long update(File wcPath, SVNRevision updateToRevision, boolean isRecursive)
            throws SVNException {
    	SVNWCClient wcClient = ourClientManager.getWCClient();
    	wcClient.doCleanup(wcPath);
    	System.out.println("Cleaned up directory");
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doUpdate(wcPath, updateToRevision, isRecursive);
    }

    private static void error(String message, Exception e){
        System.err.println(message+(e!=null ? ": "+e.getMessage() : ""));
    }
}