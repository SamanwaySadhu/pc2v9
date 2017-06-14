/**
 * This class encapsulates a set of options for configuring the PC2/CLICS "default validator".
 * Options include whether validation should be case-sensitive and/or space-sensitive, and
 * what tolerances should be used for any floating-point values in the team output processed
 * by the validator. 
 */
package edu.csus.ecs.pc2.validator.clicsValidator;

import java.io.Serializable;
import java.util.Objects;

import edu.csus.ecs.pc2.core.Constants;

/**
 * @author pc2@ecs.csus.edu
 *
 */
public class ClicsValidatorSettings implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    
    public static final String CLICS_VTOKEN_CASE_SENSITIVE = "case_sensitive";
    public static final String CLICS_VTOKEN_SPACE_CHANGE_SENSITIVE = "space_change_sensitive";
    public static final String CLICS_VTOKEN_FLOAT_RELATIVE_TOLERANCE = "float_relative_tolerance";
    public static final String CLICS_VTOKEN_FLOAT_ABSOLUTE_TOLERANCE = "float_absolute_tolerance";
    
    public static final boolean CLICS_DEFAULT_CASE_SENSITIVITY = false;
    public static final boolean CLICS_DEFAULT_SPACE_SENSITIVITY = false;
    public static final boolean CLICS_DEFAULT_IS_FLOAT_RELATIVE_TOLERANCE_SPECIFIED = false;
    public static final boolean CLICS_DEFAULT_IS_FLOAT_ABSOLUTE_TOLERANCE_SPECIFIED = false;
    public static final double CLICS_DEFAULT_FLOAT_RELATIVE_TOLERANCE = 0.0;
    public static final double CLICS_DEFAULT_FLOAT_ABSOLUTE_TOLERANCE = 0.0;
    
    private String validatorProgramName ;
    private String validatorCommandLine ;

    private boolean isCaseSensitive;
    
    private boolean isSpaceSensitive;
    
    private boolean isFloatRelativeToleranceSpecified;
    
    private boolean isFloatAbsoluteToleranceSpecified;
    
    private double floatRelativeTolerance;
    
    private double floatAbsoluteTolerance;
    
    /**
     * Constructs a ClicsValidatorSettings object with default setting values matching the 
     * defined public default constants.
     * The default values for Validator Program Name and Validator Command Line are as
     * defined by constants in class {@link Constants}.
     */
    public ClicsValidatorSettings() {

        this.validatorProgramName = Constants.CLICS_VALIDATOR_NAME;
        this.validatorCommandLine = Constants.DEFAULT_CLICS_VALIDATOR_COMMAND;
        this.isCaseSensitive = CLICS_DEFAULT_CASE_SENSITIVITY;
        this.isSpaceSensitive = CLICS_DEFAULT_SPACE_SENSITIVITY;
        this.isFloatRelativeToleranceSpecified = CLICS_DEFAULT_IS_FLOAT_RELATIVE_TOLERANCE_SPECIFIED;
        this.isFloatAbsoluteToleranceSpecified = CLICS_DEFAULT_IS_FLOAT_RELATIVE_TOLERANCE_SPECIFIED;
        this.floatRelativeTolerance = CLICS_DEFAULT_FLOAT_RELATIVE_TOLERANCE;
        this.floatAbsoluteTolerance = CLICS_DEFAULT_FLOAT_ABSOLUTE_TOLERANCE;
    }
    
    /**
     * Constructs a ClicsValidatorSettings object containing the values specified in the received String.
     * The received String is expected to contain zero or more space-separated "validator setting" options
     * as defined in the CLICS <A href="https://clics.ecs.baylor.edu/index.php/Problem_format#Default_Validator_Capabilities">
     * <I>default validator options</i></a> specification.
     * Any options not specified are set to the defaults values defined by the public default constants,
     * as if the object was constructed by calling {@link ClicsValidator#ClicsValidator()}. 
     * Any values in the received string not matching the CLICS validator options are logged but
     * otherwise silently ignored.  The same is true if valid options are specified (e.g. "float_tolerance")
     * but the required following tolerance value ("epsilon") is missing.
     */
    public ClicsValidatorSettings(String options) {
        
        //start with the default settings
        this();
        
        //see if the input string has anything to offer
        if (options!=null && options.length()>0) {
            
            //split the string into space-delimited strings
            String [] opts = options.split(" ");
            
            //process each separate string option
            for (int i=0; i<opts.length; i++) {
//                System.out.println ("DEBUG: Processing option '" + opts[i] + "'");
                
                switch (opts[i]) {
                    case CLICS_VTOKEN_CASE_SENSITIVE:
                    case "case-sensitive":
                        this.setCaseSensitive(true);
                        break;
                        
                    case CLICS_VTOKEN_SPACE_CHANGE_SENSITIVE:
                    case "space-change-sensitive":
                        this.setSpaceSensitive(true);
                        break;
                        
                    case CLICS_VTOKEN_FLOAT_RELATIVE_TOLERANCE:
                    case "float-relative-tolerance":
                        if (i<opts.length-1) {
                            try {
                                double epsilon = Double.parseDouble(opts[i+1]) ;
                                this.setFloatRelativeTolerance(epsilon);
                            } catch (NumberFormatException | NullPointerException e) {
                                System.err.println ("ClicsValidatorSettings: invalid float relative tolerance epsilon value " + e.getMessage());
                                throw new RuntimeException("ClicsValidatorSettings: invalid float relative tolerance epsilon value " + e.getMessage());
                            }
                            i++;
                        } else {
                            System.err.println ("ClicsValidatorSettings: missing float relative tolerance epsilon value");
                            throw new RuntimeException("ClicsValidatorSettings: missing float relative tolerance epsilon value");
                        }
                        break;
                        
                    case CLICS_VTOKEN_FLOAT_ABSOLUTE_TOLERANCE:
                    case "float-absolute-tolerance":
                        if (i<opts.length-1) {
                            try {
                                double epsilon = Double.parseDouble(opts[i+1]) ;
                                this.setFloatAbsoluteTolerance(epsilon);
                            } catch (NumberFormatException | NullPointerException e) {
                                System.err.println("ClicsValidatorSettings: invalid float absolute tolerance epsilon value: " + e.getMessage());
                                throw new RuntimeException("ClicsValidatorSettings: invalid float absolute tolerance epsilon value: " + e.getMessage());
                            }
                            i++;
                        } else {
                            System.err.println ("ClicsValidatorSettings: missing float absolute tolerance epsilon value");
                            throw new RuntimeException("ClicsValidatorSettings: missing float absolute tolerance epsilon value");
                        }
                        break;
                        
                    case "float_tolerance":
                    case "float-tolerance":
                        if (i<opts.length-1) {
                            try {
                                double epsilon = Double.parseDouble(opts[i+1]) ;
                                this.setFloatRelativeTolerance(epsilon);
                                this.setFloatAbsoluteTolerance(epsilon);
                            } catch (NumberFormatException | NullPointerException e) {
                                System.err.println ("ClicsValidatorSettings: invalid float tolerance epsilon value: " + e.getMessage());
                                throw new RuntimeException("ClicsValidatorSettings: invalid float tolerance epsilon value: " + e.getMessage());
                            }
                            i++;
                        } else {
                            System.err.println ("ClicsValidatorSettings: missing float tolerance epsilon value");
                            throw new RuntimeException("ClicsValidatorSettings: missing float tolerance epsilon value");
                        }
                        break;
                        
                    default:
                        System.err.println ("ClicsValidatorSettings: warning: unknown option '" + opts[i] +"'");
                        throw new RuntimeException("ClicsValidatorSettings: unknown option '" + opts[i] +"'");

                }//end switch
            }//end for each option field
        }
    }

    /**
     * @return the validatorProgramName
     */
    public String getValidatorProgramName() {
        return validatorProgramName;
    }

    /**
     * Sets the name of the program to be executed in association with this Settings object.
     * 
     * @param validatorProgramName the validatorProgramName to set
     */
    public void setValidatorProgramName(String validatorProgramName) {
        this.validatorProgramName = validatorProgramName;
    }

    /**
     * @return the validatorCommandLine
     */
    public String getValidatorCommandLine() {
        return validatorCommandLine;
    }

    /**
     * Sets the Validator Command Line to be used to invoke the Validator associated with this Settings object.
     * 
     * @param validatorCommandLine the validatorCommandLine to set
     */
    public void setValidatorCommandLine(String validatorCommandLine) {
        this.validatorCommandLine = validatorCommandLine;
    }

    /**
     * @return whether or not the isCaseSensitive option is set.
     */
    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    /**
     * Sets the case-sensitivity option.
     * @param caseSensitive the case-sensitivity value to set
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.isCaseSensitive = caseSensitive;
    }

    /**
     * @return whether or not the isSpaceSensitive option is set.
     */
    public boolean isSpaceSensitive() {
        return isSpaceSensitive;
    }

    /**
     * Sets the space-sensitivity option.
     * @param spaceSensitive the space-sensitivity value to set
     */
    public void setSpaceSensitive(boolean spaceSensitive) {
        this.isSpaceSensitive = spaceSensitive;
    }

    /**
     * Returns the current float relative tolerance value if one has been specified; 
     * if no float relative tolerance has been specified, returns the default relative tolerance value
     * defined by {@link ClicsValidatorSettings#CLICS_DEFAULT_FLOAT_RELATIVE_TOLERANCE}
     * (normally zero).
     * @return the defined floatRelativeTolerance, or the default float relative tolerance if
     *          no float relative tolerance has been set
     */
    public double getFloatRelativeTolerance() {
        if (this.isFloatRelativeToleranceSpecified()) {
            return floatRelativeTolerance;
        } else {
            return CLICS_DEFAULT_FLOAT_RELATIVE_TOLERANCE;
        }
    }

    /**
     * Sets the float relative tolerance to the specified value, and marks this
     * ClicsValidatorSettings object has having a specified float relative tolerance.
     * 
     * @param floatRelativeTolerance the float relative tolerance value to set
     */
    public void setFloatRelativeTolerance(double floatRelativeTolerance) {
        this.floatRelativeTolerance = floatRelativeTolerance;
        this.isFloatRelativeToleranceSpecified = true;
    }
    
    /**
     * Marks this ClicsValidatorSettings object as not having any specified float relative tolerance.
     */
    public void disableFloatRelativeTolerance() {
        this.isFloatRelativeToleranceSpecified = false ;
    }
    
    /**
     * Returns the current float absolute tolerance value if one has been specified; 
     * if no float absolute tolerance has been specified, returns the default absolute tolerance value
     * defined by {@link ClicsValidatorSettings#CLICS_DEFAULT_FLOAT_ABSOLUTE_TOLERANCE}
     * (normally zero).
     * @return the defined float absolute tolerance, or the default float absolute tolerance if
     *          no float absolute tolerance has been set
     */
    public double getFloatAbsoluteTolerance() {
        if (this.isFloatAbsoluteToleranceSpecified()) {
            return floatAbsoluteTolerance;
        } else {
            return CLICS_DEFAULT_FLOAT_ABSOLUTE_TOLERANCE ;
        }
    }

    /**
     * Sets the float absolute tolerance to the specified value, and marks this
     * ClicsValidatorSettings object has having a float absolute tolerance value.
     * 
     * @param floatAbsoluteTolerance the float absolute tolerance value to set
     */
    public void setFloatAbsoluteTolerance(double floatAbsoluteTolerance) {
        this.floatAbsoluteTolerance = floatAbsoluteTolerance;
        this.isFloatAbsoluteToleranceSpecified = true;
    }

    /**
     * Marks this ClicsValidatorSettings object as not having any specified float absolute tolerance.
     */
    public void disableFloatAbsoluteTolerance() {
        this.isFloatAbsoluteToleranceSpecified = false ;
    }
    
    /**
     * Returns the flag indicating whether float relative tolerance has been specified for this collection of settings.
     * @return the isFloatRelativeToleranceSpecified flag
     */
    public boolean isFloatRelativeToleranceSpecified() {
        return isFloatRelativeToleranceSpecified;
    }


    /**
     * Returns the flag indicating whether float absolute tolerance has been specified for this collection of settings.
     * @return the isFloatAbsoluteToleranceSpecified flag
     */
    public boolean isFloatAbsoluteToleranceSpecified() {
        return isFloatAbsoluteToleranceSpecified;
    }

    /**
     * Returns true if the default validator settings in this object match those in
     * the "other" object; false otherwise.
     * Note that if both objects agree that float absolute and/or relative tolerance is NOT
     * specified, then the objects will otherwise compare as "equal" regardless of the actual values
     * stored in the absolute and/or relative float tolerance fields (in other words, if both objects
     * agree that a tolerance is "not specified", then no check against the corresponding values is
     * performed).
     * 
     * @param other -- the object against which to compare this object's settings
     * @return true if this object matches the other object
     */
    @Override
    public boolean equals (Object obj) {
        
        if (obj==null || !(obj instanceof ClicsValidatorSettings)) {
            return false;
        }
        
        ClicsValidatorSettings other = (ClicsValidatorSettings) obj;
        
        //check whether one validator program name is null while the other is not
        if (this.validatorProgramName==null ^ other.validatorProgramName==null) {
            return false;
        }
        
        //check whether, if both names are non-null, they are the same
        if (this.validatorProgramName != null) {
            if (!(this.validatorProgramName.equals(other.validatorProgramName))) {
                return false;
            }
        }

        //check whether one validator validator Command Line is null while the other is not
        if (this.validatorCommandLine==null ^ other.validatorCommandLine==null) {
            return false;
        }
        
        //check whether, if both command lines are non-null, they are the same
        if (this.validatorCommandLine != null) {
            if (!(this.validatorCommandLine.equals(other.validatorCommandLine))) {
                return false;
            }
        }
        
        if (this.isCaseSensitive()!=other.isCaseSensitive()) {
            return false;
        }
        
        if (this.isSpaceSensitive()!=other.isSpaceSensitive()) {
            return false;
        }
        
        if (this.isFloatAbsoluteToleranceSpecified() != other.isFloatAbsoluteToleranceSpecified()) {
            return false;
        }
        
        if (this.isFloatRelativeToleranceSpecified() != other.isFloatRelativeToleranceSpecified()) {
            return false;
        }
        
        if (this.isFloatAbsoluteToleranceSpecified() && this.getFloatAbsoluteTolerance()!=other.getFloatAbsoluteTolerance()) {
            return false;
        }
        
        if (this.isFloatRelativeToleranceSpecified() && this.getFloatRelativeTolerance()!=other.getFloatRelativeTolerance()) {
            return false;
        }
        // remember to update hashCode if new fields are added here
       
        return true;
        
    }

    @Override
    public int hashCode() {
        return Objects.hash(validatorProgramName,validatorCommandLine,isCaseSensitive,isSpaceSensitive,isFloatAbsoluteToleranceSpecified,isFloatRelativeToleranceSpecified);
    }
    
    /**
     * Returns a {@link ClicsValidatorSettings} object which is a clone of this object.
     */
    public ClicsValidatorSettings clone() {
        ClicsValidatorSettings clone = new ClicsValidatorSettings();
        clone.setCaseSensitive(this.isCaseSensitive());
        clone.setSpaceSensitive(this.isSpaceSensitive());
        clone.setFloatAbsoluteTolerance(this.getFloatAbsoluteTolerance());
        clone.setFloatRelativeTolerance(this.getFloatRelativeTolerance());
        clone.isFloatAbsoluteToleranceSpecified = this.isFloatAbsoluteToleranceSpecified;
        clone.isFloatRelativeToleranceSpecified = this.isFloatRelativeToleranceSpecified;
        return clone;
    }
    
    /**
     * Returns a String representation of this {@link ClicsValidatorSettings} object.
     * The format of the string is as if the validator options were specified on a command line
     * as per the CLICS Default Validator invocation specification; that is, a series of space-delimited
     * options.  Note that this means that if an option is currently not specified, the option will not
     * be present in the returned string.  This in turn means, for example, that the string returned for
     * a ClicsValidatorSettings object for which no options have been specified will be the empty string.
     */
    public String toString() {
        String retStr = "";
        if (this.isCaseSensitive()) {
            retStr += "case_sensitive";
        }
        if (this.isSpaceSensitive()) {
            if (retStr.length()>0) {
                retStr += " ";
            }
            retStr += "space_change_sensitive";
        }
        if (this.isFloatAbsoluteToleranceSpecified()) {
            if (retStr.length()>0) {
                retStr += " ";
            }
            retStr += "float_absolute_tolerance ";
            retStr += Double.toString(getFloatAbsoluteTolerance());
        }
        if (this.isFloatRelativeToleranceSpecified()) {
            if (retStr.length()>0) {
                retStr += " ";
            }
            retStr += "float_relative_tolerance ";
            retStr += Double.toString(getFloatRelativeTolerance());
        }
        return retStr;
    }
}
