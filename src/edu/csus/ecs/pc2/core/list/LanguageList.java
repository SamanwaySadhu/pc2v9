package edu.csus.ecs.pc2.core.list;

import edu.csus.ecs.pc2.core.model.Language;

/**
 * Maintain a list of {@link edu.csus.ecs.pc2.core.model.Language}s.
 * 
 * @version $Id$
 * @author pc2@ecs.csus.edu
 */

// $HeadURL$
public class LanguageList extends ElementList {

    /**
     * 
     */
    private static final long serialVersionUID = -7236148308850761761L;

    public static final String SVN_ID = "$Id$";

    // private LanguageComparator languageComparator = new LanguageComparator();

    /**
     * 
     * @param language
     *            {@link Language} to be added.
     */
    public void add(Language language) {
        super.add(language);

    }

    /**
     * Return list of Languages.
     * 
     * @return list of {@link Language}.
     */
    public Language[] getList() {
        Language[] theList = new Language[size()];

        if (theList.length == 0) {
            return theList;
        }

        theList = (Language[]) values().toArray(new Language[size()]);
        return theList;
    }
}
