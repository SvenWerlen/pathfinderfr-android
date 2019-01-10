package org.pathfinderfr.app.util;

import android.util.Pair;

import org.pathfinderfr.app.database.entity.Spell;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellFilter {

    public List<Spell> spells;

    private HashSet<String> filterSchool;
    private HashSet<String> filterClass;
    private HashSet<String> filterLevel;


    public SpellFilter(List<Spell> spells, String preferences) {
        this.spells = spells;
        filterSchool = new HashSet<>();
        filterClass = new HashSet<>();
        filterLevel = new HashSet<>();

        System.out.println("PREFS = " + preferences);

        // load preferences
        if(preferences != null) {
            String[] prefs = preferences.split(":");
            if(prefs.length == 3) {
                if(prefs[0].length()>0) {
                    filterSchool.addAll(Arrays.asList(prefs[0].split(",")));
                }
                if(prefs[1].length()>0) {
                    filterClass.addAll(Arrays.asList(prefs[1].split(",")));
                }
                if(prefs[2].length()>0) {
                    filterLevel.addAll(Arrays.asList(prefs[2].split(",")));
                }
            }
        }
    }

    /**
     * This function cleans the "school" data to avoid duplicates
     * @param school original value (from import)
     * @return clean value
     */
    private static String cleanSchool(String school) {
        // only take first word
        Pattern pattern = Pattern.compile("([A-zÀ-ú]+).*");
        Matcher matcher = pattern.matcher(school);
        if (matcher.find())
        {
            String clean = matcher.group(1);
            // capitalize first letter only
            return clean.substring(0, 1).toUpperCase() + clean.substring(1);
        }
        return null;
    }

    /**
     * @return the list of unique schools ordered by name
     */
    public List<String> getSchools() {
        Set<String> schools = new HashSet<String>();
        for( Spell s: spells) {
            String clean = cleanSchool(s.getSchool());
            if(clean != null) {
                schools.add(clean);
            }
        }
        List<String> uniqSchools = new ArrayList<>();
        uniqSchools.addAll(schools);
        Collator collator = Collator.getInstance();
        Collections.sort(uniqSchools,collator);
        return uniqSchools;
    }


    /**
     * This function cleans the "class" data to avoid duplicates
     *
     * Examples:
     *   Alch 3,  Conj 3, Ens/Mag 3, Magus 3, Sor 3
     *   ensorceleur/magicien 3, sorcière 3
     *
     * @param cl (class) original value (from import)
     * @return clean value
     */
    private static List<Pair<String,String>> cleanClasses(String cl) {
        List<String> classes = new ArrayList<>();
        // Split by comma ',' and eventually by slash '/'
        String[] regex = cl.split(",");
        for(String s : regex) {
            s = s.toLowerCase().trim();
            if(s.indexOf('/') > 0) {
                classes.addAll(Arrays.asList(s.split("/")));
            } else {
                classes.add(s);
            }
        }
        // clean data by removing level and spaces, and reduce to 3-letters acronym
        List<Pair<String,String>> clean = new ArrayList<>();
        for(String c: classes) {
            Pattern pattern = Pattern.compile("([A-zÀ-ú]+).*([0-9])");
            Matcher matcher = pattern.matcher(c);
            if (matcher.find())
            {
                String cleanClass = matcher.group(1);
                String cleanLevel = matcher.group(2);
                if(cleanClass.length() > 3) {
                    cleanClass = cleanClass.substring(0,3);
                }
                // capitalize first letter only
                cleanClass = cleanClass.substring(0, 1).toUpperCase() + cleanClass.substring(1);

                clean.add(new Pair<String,String>(cleanClass,cleanLevel));
            }
        }

        return clean;
    }

    /**
     * @return the list of unique classes ordered by name
     */
    public List<String> getClasses() {
        Set<String> classes = new HashSet<String>();
        for( Spell s: spells) {
            List<Pair<String,String>> clean = cleanClasses(s.getLevel());
            if(clean != null) {
                for(Pair<String,String> p : clean) {
                    classes.add(p.first);
                }
            }
        }
        List<String> uniqClass = new ArrayList<>();
        uniqClass.addAll(classes);
        Collator collator = Collator.getInstance();
        Collections.sort(uniqClass,collator);
        return uniqClass;
    }

    public void clearFilters() {
       filterSchool.clear();
       filterClass.clear();
       filterLevel.clear();
    }

    public void addFilterSchool(String school) {
        filterSchool.add(school.toLowerCase());
    }

    public void addFilterClass(String cl) {
        filterClass.add(cl.toLowerCase());
    }

    public void addFilterLevel(String level) {
        filterLevel.add(level.toLowerCase());
    }

    /**
     * @param school spell school
     * @return true if filter is enabled for given school. false if disabled or "All" selected
     */
    public boolean isFilterSchoolEnabled(String school) {
        return filterSchool.contains(school.toLowerCase());
    }

    public boolean hasFilterSchool() {
        return !filterSchool.isEmpty();
    }

    /**
     * @param cl spell class
     * @return true if filter is enabled for given class. false if disabled or "All" selected
     */
    public boolean isFilterClassEnabled(String cl) {
        return filterClass.contains(cl.toLowerCase());
    }

    public boolean hasFilterClass() {
        return !filterClass.isEmpty();
    }

    /**
     * @param level spell level
     * @return true if filter is enabled for given level. false if disabled or "All" selected
     */
    public boolean isFilterLevelEnabled(String level) {
        return filterLevel.contains(level.toLowerCase());
    }

    public boolean hasFilterLevel() {
        return !filterLevel.isEmpty();
    }

    public List<Spell> getFilteredList() {
        List<Spell> filtered = new ArrayList<>();
        for(Spell s: spells) {
            boolean ok = true;
            if(ok && !filterSchool.isEmpty()) {
                boolean found = false;
                for(String f: filterSchool) {
                    if(s.getSchool().toLowerCase().indexOf(f) >= 0) {
                        found = true;
                        break;
                    }
                }
                ok = ok && found;
            }
            // special case: class & level combined together
            if(ok && !filterClass.isEmpty() && !filterLevel.isEmpty()) {
                List<Pair<String, String>> classes = cleanClasses(s.getLevel());
                boolean found = false;
                for(Pair<String, String> c: classes) {
                    if(filterClass.contains(c.first.toLowerCase()) && filterLevel.contains(c.second)) {
                        found = true;
                        break;
                    }
                }
                ok = ok && found;
            }
            // filter only on class
            if(ok && !filterClass.isEmpty() && filterLevel.isEmpty()) {
                boolean found = false;
                for(String f: filterClass) {
                    if(s.getLevel().toLowerCase().indexOf(f) >= 0) {
                        found = true;
                        break;
                    }
                }
                ok = ok && found;
            }
            // filter only on level
            if(ok && filterClass.isEmpty() && !filterLevel.isEmpty()) {
                boolean found = false;
                for(String f: filterLevel) {
                    if(s.getLevel().indexOf(f) >= 0) {
                        found = true;
                        break;
                    }
                }
                ok = ok && found;
            }
            if(ok) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    /**
     * @return filters as String (useful for import/export as preferences)
     */
    public String generatePreferences() {
        StringBuffer buf = new StringBuffer();
        if(!filterSchool.isEmpty()) {
            for (String s : filterSchool) {
                buf.append(s).append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append(':');
        if(!filterClass.isEmpty()) {
            for (String s : filterClass) {
                buf.append(s).append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append(':');
        if(!filterLevel.isEmpty()) {
            for (String s : filterLevel) {
                buf.append(s).append(',');
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
}
