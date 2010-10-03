package edu.csus.ecs.pc2.profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IStorage;
import edu.csus.ecs.pc2.core.exception.ProfileException;
import edu.csus.ecs.pc2.core.model.Profile;
import edu.csus.ecs.pc2.core.security.FileSecurity;
import edu.csus.ecs.pc2.core.security.FileSecurityException;

/**
 * Profiles Manager.
 * 
 * Provides ways to:
 * <li>load/store text profile information
 * <li>a way to determine whether to prompt user for contest password
 * <li>a ways to save/get the default (current) profile
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ProfileManager {

    public static final String DEFAULT_PROFILE_KEY = "current";

    /**
     * Filename for index of profiles.
     */
    public static final String PROFILE_INDEX_FILENAME = "profiles.properties";

    private String delimiter = ",";
    
    /**
     * Create Profile from data in filename.
     * 
     * 
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws ProfileLoadException
     */
    public Profile[] load(String filename) throws IOException, ProfileLoadException {

        if (new File(filename).exists()) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(filename));
            return toProfiles(properties);
        } else {
            throw new FileNotFoundException(filename);
        }
    }
    
    public Profile[] load() throws IOException, ProfileLoadException {
        return load(PROFILE_INDEX_FILENAME);
    }
    
    /**
     * Add a new profile to existing profiles.
     * 
     * Will create and set as default the new profile if not
     * profiles are stored in the profiles.properties file.      
     * @param filename
     * @param profile
     * @throws IOException
     * @throws ProfileLoadException
     */
    public void add (String filename, Profile profile) throws IOException, ProfileLoadException{
        
        if (hasDefaultProfile()) {
            
            Profile[] profiles = load(filename);
            Profile defaultProfile = getDefaultProfile(filename);
            store(filename, profiles, defaultProfile);
            
        } else {
            storeDefaultProfile(filename, profile);
        }
    }
    
    /**
     * Add a new profile to existing profiles.
     * 
     * Will create and set as default the new profile if not
     * profiles are stored in the profiles.properties file. 
     * 
     * @param profile
     * @throws IOException
     * @throws ProfileLoadException
     */
    public void add (Profile profile) throws IOException, ProfileLoadException{
        add (PROFILE_INDEX_FILENAME, profile);
    }

    /**
     * Determines whether the input profile can be read using contest password.
     * 
     * @param profile
     * @return true if profile
     * @throws ProfileException
     */
    public boolean isProfileAvailable(Profile profile, char[] contestPassword) throws ProfileException {
        return getProfileStorage(profile, contestPassword) != null;
    }
    
    /**
     * Returns storage location for input profile.
     * 
     * Can be used to check whether contest password is valid.
     * 
     * @param profile
     * @param contestPassword
     * @return
     * @throws ProfileException
     */
    public IStorage getProfileStorage(Profile profile, char[] contestPassword) throws ProfileException {

        if (profile == null) {
            throw new IllegalArgumentException("Profile can not be null");
        }

        String profilePath = profile.getProfilePath();

        if (new File(profilePath).isDirectory()) {

            String dbDirectory = profilePath + File.separator + "db." + profile.getSiteNumber();
            if (new File(dbDirectory).isDirectory()) {
                profilePath = dbDirectory;
            } else {
                throw new ProfileException(profile, "Profile DB directory does not exist: " + dbDirectory);
            }

            FileSecurity fileSecurity = new FileSecurity(profilePath);

            try {
                boolean validPassword = fileSecurity.verifyPassword(contestPassword);
                if (validPassword) {
                    return fileSecurity;
                } else {
                    throw new ProfileException(profile, "Profile contest password is not correct");
                }
            } catch (FileSecurityException e) {
                throw new ProfileException(profile, e);
            }
        } else {
            throw new ProfileException(profile, "Profile directory does not exist: " + profilePath);
        }
    }
    
    public boolean hasDefaultProfile (){
        return hasDefaultProfile(PROFILE_INDEX_FILENAME);
    }
    /**
     * Is there a default profile defined ?
     * @return
     */
    public boolean hasDefaultProfile (String filename){
        if (new File(filename).exists()) {
            try {
                return getDefaultProfile() != null;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public Profile getDefaultProfile() throws IOException, ProfileLoadException {
        return getDefaultProfile(PROFILE_INDEX_FILENAME);
    }
    
    public Profile getDefaultProfile(String filename) throws IOException, ProfileLoadException {
        if (new File(filename).exists()) {

            Profile profile = null;

            Properties properties = new Properties();
            properties.load(new FileInputStream(filename));

            String key = properties.getProperty(DEFAULT_PROFILE_KEY);
            if (key != null) {
                String profileLine = properties.getProperty(key);
                return toProfile(key, profileLine);
            }

            return profile;

            //           
            // # General PPF Form
            // #
            // [profiles]
            // current=<prof_id>
            // <prof_id>="<title>","<description>","<profile path>",<client_type>[=first]
            // # eof
            //

        } else {
            throw new FileNotFoundException(filename);
        }
    }

    /**
     * Create a profile form an input line/String.
     * 
     * <prof_id>="<title>","<description>","<profile path>",<client_type>[=first]
     * 
     * @param profileLine
     * @param profileLine 
     * @return
     * @throws ProfileLoadException
     */
    private Profile toProfile(String key, String profileLine) throws ProfileLoadException {

        if (profileLine == null) {
            return null;
        }

        String[] fields = profileLine.split(delimiter);

        if (fields.length < 3) {
            throw new IllegalArgumentException("Too few fields for line: " + profileLine);
        }

        // 0 "<title>",
        // 1 "<description>",
        // 2 "<profile path>",
        // 3 <client_type>[=first]

        String title = stripQuote(fields[0]);
        String description = stripQuote(fields[1]);
        String path = stripQuote(fields[2]);
        // String extraInfo = fields[3];

        if (title == null || title.length() == 0) {
            throw new ProfileLoadException("No title found in: " + profileLine);
        }

        Profile profile = new Profile(title);

        if (description != null) {
            profile.setDescription(description);
        }

        if (path == null || path.length() == 0) {
            throw new ProfileLoadException("No path found in: " + profileLine);
        }

        profile.setProfilePath(path);
        profile.setContestId(key);

        return profile;
    }

    /**
     * Strip all double quotes from start and end of string.
     * 
     * @param string
     * @return
     */
    protected String stripQuote(String string) {

        if (string == null) {
            return null;
        }

        StringBuffer buffer = new StringBuffer(string);

        if (buffer.charAt(0) == '"') {
            buffer.deleteCharAt(0);
        }

        int idx = buffer.lastIndexOf("\"");

        if (idx > -1) {
            buffer.deleteCharAt(idx);
        }

        return buffer.toString();
    }

    /**
     * Return a list of Profiles.
     * 
     * @param properties
     * @return
     * @throws ProfileLoadException
     */
    private Profile[] toProfiles(Properties properties) throws ProfileLoadException {

        Vector<Profile> list = new Vector<Profile>();

        String[] keys = (String[]) properties.keySet().toArray(new String[properties.keySet().size()]);
        for (String key : keys) {
            String profileLine = properties.getProperty(key);
            String[] fields = profileLine.split(delimiter);
            if (fields.length > 2) {
                Profile profile = toProfile(key, profileLine);
                list.add(profile);
            }
        }

        return (Profile[]) list.toArray(new Profile[list.size()]);
    }
    
    /**
     * Store the profiles at the default location.
     * 
     * @param profiles
     * @param defaultProfile
     * @return
     * @throws IOException
     */
    public boolean store( Profile[] profiles, Profile defaultProfile) throws IOException {
        return this.store(PROFILE_INDEX_FILENAME, profiles, defaultProfile);
    }
    
    public boolean storeDefaultProfile(Profile defaultProfile) throws IOException {
        return storeDefaultProfile(PROFILE_INDEX_FILENAME, defaultProfile);
    }
    
    public boolean storeDefaultProfile(String filename, Profile defaultProfile) throws IOException {
        Profile[] profiles = null;

        try {
            profiles = load();
        } catch (Exception e) {
            profiles = new Profile[1];
            profiles[0] = defaultProfile;
        }

        return this.store(filename, profiles, defaultProfile);
    }


    /**
     * Store list of profiles.
     * 
     * @param filename
     * @param profiles
     * @param defaultProfile
     * @return
     * @throws IOException
     */
    public boolean store(String filename, Profile[] profiles, Profile defaultProfile) throws IOException {
        Properties properties = new Properties();

        VersionInfo versionInfo = new VersionInfo();

        for (Profile profile : profiles) {
            // <prof_id>="<title>","<description>","<profile path>",<client_type>[=first]
            String line = quoteString(profile.getName()) + delimiter + quoteString(profile.getDescription()) + delimiter + quoteString(profile.getProfilePath()) + delimiter;
            properties.put(profile.getElementId().toString(), line);
        }
        
        // Add default profile into list if it isn't already there.
        if (! properties.containsKey(defaultProfile.getElementId().toString())){
            String line = quoteString(defaultProfile.getName()) + delimiter + quoteString(defaultProfile.getDescription()) + delimiter + quoteString(defaultProfile.getProfilePath()) + delimiter;
            properties.put(defaultProfile.getElementId().toString(), line);
        }

        properties.put(DEFAULT_PROFILE_KEY, defaultProfile.getElementId().toString());

        properties.store(new FileOutputStream(filename), "Created by PC^2 Version " + versionInfo.getVersionNumber() + " " + versionInfo.getBuildNumber()
                + " $Id$ ");

        return true;
    }

    private String quoteString(String name) {
        return "\"" + name + "\"";
    }

    /**
     * Merges profiles (merges profiles into currentProfiles).
     * 
     * @param currentProfiles
     * @param profiles
     * @return
     */
    public Profile[] mergeProfiles(Profile[] currentProfiles, Profile[] profiles) {

        Hashtable<String, Profile> table = new Hashtable<String, Profile>();

        if (currentProfiles != null) {
            for (Profile profile : currentProfiles) {
                table.put(profile.getProfilePath(), profile);
            }
        }

        if (profiles != null) {
            for (Profile profile : profiles) {
                table.put(profile.getProfilePath(), profile);
            }
        }

        return (Profile[]) table.values().toArray(new Profile[table.values().size()]);
    }
    
    public static Profile createNewProfile() {
        Profile profile = new Profile("Contest");
        profile.setDescription("Real Contest");
        return profile;
    }

}
