/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xliff2n3;

/**
 * Container for informational data about the conversion
 */
public class LoggerStatus {

    public enum Stage {
        Initializing,
        Parsing,
        Validating,
        Converting,
        Merging,
    };

    private String infoText ="";

    /**
     * True if completed successfully
     */
    private boolean success = false;

    /**
     * The current processing stage
     */
    private Stage stage = Stage.Initializing;

    /**
     * Accessor for property success
     * @return True if completed successfully, false otherwise
     */
    public boolean succeeded() {
        return this.success;
    }

    /**
     * Accessor for property success
     * @value True if completed successfully, false otherwise
     */
    public void setSucceeded(boolean value) {
        this.success = value;
    }

    /**
     * The current processing stage
     */
    public void enterStage(Stage stage) {
        this.stage = stage;
        this.infoText += String.format("%s\n", stage.toString());
    }

    /**
     * Exception thrown, if any
     */
    public void reportException(Exception exception) {
        this.infoText += exception.toString() + "\n";
    }

    /**
     * Get the output text
     */
    public String getOutput() {
        if (!this.success) {
            return String.format("Current stage:%s\nOutput:%s", this.stage.toString(), this.infoText);
        }
        return this.infoText;
    }

    /**
     * Append text to the output (used to spew the N3 conversion results)
     * @param string Text to append to the output
     */
    public void append(String string) {
        this.infoText += string + "\n";
    }

}
