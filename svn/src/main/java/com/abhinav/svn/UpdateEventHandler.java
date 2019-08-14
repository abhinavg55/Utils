package com.abhinav.svn;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNStatusType;

public class UpdateEventHandler implements ISVNEventHandler {
	
	private static List<String> ignoredFiles = new LinkedList<String>();
	
	static {
		ignoredFiles.add("500020000100_DDL_Index_on_FK.sql");
		ignoredFiles.add("500050000360_proc_Employer_Ein_backword.sql");
		ignoredFiles.add("510010000370_DML_RENAME_PROSPECT_CLIENT_COLUMN.sql");
		ignoredFiles.add("510040000850_DML_transparency_report_config_revert.txt");
		ignoredFiles.add("510040001220_DDL_auto_schedule_CLASS_RECONCILLIATION_REPORT.sql");
		ignoredFiles.add("510040001260_DDL_auto_schedule_CLASS_RECONCILLIATION_REPORT.sql");
		ignoredFiles.add("510040001300_DDL_auto_schedule_CLASS_RECONCILLIATION_REPORT.sql");
		ignoredFiles.add("511010000510_DML_TYPES_census_report.sql");
		ignoredFiles.add("511020000090_census_report_name_update.SQL");
		ignoredFiles.add("511020000100_census_report_file_name_update.SQL");
		ignoredFiles.add("512030000170_STD_LTD_enrollment_backward_script.sql");
		ignoredFiles.add("512030000840_DML_insert_enrollment_mat_SEP.sql");
		ignoredFiles.add("521020000090_DDL_Increase_Length_of_employee_CF_Value.sql");
		ignoredFiles.add("521020000360_Equal_employment_oppotunity_report.sql");

	}
	
	private String path;

	private String gmailUserName;

	private String gmailPassword;
	
    public UpdateEventHandler(String path, String gmailUserName, String gmailPassword) {
    	this.path=path;
    	this.gmailUserName = gmailUserName;
    	this.gmailPassword = gmailPassword;
    	
	}

	private List<List<String>> populateFileNames() {
		List<List<String>> files = new LinkedList<List<String>>();
		String patchFolderPath = path + "\\Patch\\PHIX2.0_PH1_RC_1.0";
		String patchProdFolderPath = path + "\\patch-prod\\PHIX2.0_PH1_RC_1.7";
		if (path.contains("microservices")) {
			patchFolderPath = path + "\\patch";
			patchProdFolderPath = path + "\\patch";
		}

		String[] patchFolderFiles = new File(patchFolderPath).list();
		List<String> patchFolderFilesList = new LinkedList<String>(Arrays.asList(patchFolderFiles));
		patchFolderFilesList.removeAll(ignoredFiles);
		files.add(patchFolderFilesList);
		System.out.println("Patch folder size: " + patchFolderFiles.length);
		
		String[] patchProdFolderLength = new File(patchProdFolderPath).list();
		List<String> patchProdFolderFilesList = new LinkedList<String>(Arrays.asList(patchProdFolderLength));
		files.add(patchProdFolderFilesList);
		System.out.println("Patch-prod folder size: " + patchProdFolderLength.length);
		return files;
	}

	/*
     * progress  is  currently  reserved  for future purposes and now is always
     * ISVNEventHandler.UNKNOWN  
     */
    public void handleEvent(SVNEvent event, double progress) {
        /*
         * Gets the current action. An action is represented by SVNEventAction.
         * In case of an update an  action  can  be  determined  via  comparing 
         * SVNEvent.getAction() and SVNEventAction.UPDATE_-like constants. 
         */
        SVNEventAction action = event.getAction();
        String pathChangeType = " ";
        if (action == SVNEventAction.UPDATE_ADD) {
            /*
             * the item was added
             */
            pathChangeType = "A";
        } else if (action == SVNEventAction.UPDATE_DELETE) {
            /*
             * the item was deleted
             */
            pathChangeType = "D";
        } else if (action == SVNEventAction.UPDATE_UPDATE) {
            /*
             * Find out in details what  state the item is (after  having  been 
             * updated).
             * 
             * Gets  the  status  of  file/directory  item   contents.  It   is 
             * SVNStatusType  who contains information on the state of an item.
             */
            SVNStatusType contentsStatus = event.getContentsStatus();
            if (contentsStatus == SVNStatusType.CHANGED) {
                /*
                 * the  item  was  modified in the repository (got  the changes 
                 * from the repository
                 */
                pathChangeType = "U";
            }else if (contentsStatus == SVNStatusType.CONFLICTED) {
                /*
                 * The file item is in  a  state  of Conflict. That is, changes
                 * received from the repository during an update, overlap  with 
                 * local changes the user has in his working copy.
                 */
                pathChangeType = "C";
            } else if (contentsStatus == SVNStatusType.MERGED) {
                /*
                 * The file item was merGed (those  changes that came from  the 
                 * repository  did  not  overlap local changes and were  merged 
                 * into the file).
                 */
                pathChangeType = "G";
            }
        } else if (action == SVNEventAction.UPDATE_EXTERNAL) {
            /*for externals definitions*/
            System.out.println("Fetching external item into '"
                    + event.getFile().getAbsolutePath() + "'");
            System.out.println("External at revision " + event.getRevision());
            return;
        } else if (action == SVNEventAction.UPDATE_COMPLETED) {

            System.out.println("At revision " + event.getRevision());
            List<List<String>> finalFiles = populateFileNames();
            List<String> differenceFound = getFilesDifference(finalFiles);
			if (differenceFound.size() != 0) {
            	new Report(gmailUserName, gmailPassword, path).sendMail(differenceFound);
            } else {
            	System.out.println("No change...");
            }
            return;
        } else if (action == SVNEventAction.ADD){
            return;
        } else if (action == SVNEventAction.DELETE){
            return;
        } else if (action == SVNEventAction.LOCKED){
            return;
        } else if (action == SVNEventAction.LOCK_FAILED){
            return;
        }

        /*
         * Now getting the status of properties of an item. SVNStatusType  also
         * contains information on the properties state.
         */
        SVNStatusType propertiesStatus = event.getPropertiesStatus();
        /*
         * At first consider properties are normal (unchanged).
         */
        String propertiesChangeType = " ";
        if (propertiesStatus == SVNStatusType.CHANGED) {
            /*
             * Properties were updated.
             */
            propertiesChangeType = "U";
        } else if (propertiesStatus == SVNStatusType.CONFLICTED) {
            /*
             * Properties are in conflict with the repository.
             */
            propertiesChangeType = "C";
        } else if (propertiesStatus == SVNStatusType.MERGED) {
            /*
             * Properties that came from the repository were  merged  with  the
             * local ones.
             */
            propertiesChangeType = "G";
        }

        /*
         * Gets the status of the lock.
         */
        String lockLabel = " ";
        SVNStatusType lockType = event.getLockStatus();
        
        if (lockType == SVNStatusType.LOCK_UNLOCKED) {
            /*
             * The lock is broken by someone.
             */
            lockLabel = "B";
        }
    }
    
    private List<String> getFilesDifference(List<List<String>> finalFiles) {
    	List<String> patchFolderFiles = finalFiles.get(0);
    	List<String> patchProdFiles = finalFiles.get(1);
    	patchFolderFiles.removeAll(patchProdFiles);
    	return patchFolderFiles;
	}

	public void checkCancelled() throws SVNCancelException {
    }

}