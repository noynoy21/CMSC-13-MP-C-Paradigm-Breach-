package cmsc13.ParadigmBreach.util;

public class StringsConfig {
    private static StringsConfig instance;

    private StringsConfig() {}

    public static StringsConfig get() {
        if (instance == null) {
            instance = new StringsConfig();
        }

        return instance;
    }

    private final String gameOverError = """
                                                SystemCore.java:0: error: compilation aborted
                                                  reason: critical system state is no longer recoverable

                                                SystemCore.java:117: error: cannot find symbol
                                                        if (remainingLives > 0) {
                                                            ^
                                                symbol:   variable remainingLives
                                                location: class SystemCore
                                                detail: all available life reserves have been exhausted

                                                SystemCore.java:118: error: unreachable statement
                                                            resumeSession();
                                                            ^
                                                detail: no valid execution path remains

                                                SystemCore.java:203: error: cannot access WardenFirewall
                                                        WardenFirewall.openExit();
                                                        ^
                                                bad class file: ./WardenFirewall.class
                                                    class file contains corrupted access controls
                                                    Please remove or make sure it appears in the correct subdirectory

                                                SystemCore.java:204: error: WardenFirewall has private access
                                                        WardenFirewall.openExit();
                                                        ^
                                                detail: the Paradigm Wardens have revoked your access credentials

                                                SystemCore.java:205: error: cannot find symbol
                                                        WardenFirewall.openExit();
                                                                    ^
                                                symbol:   method openExit()
                                                location: class WardenFirewall
                                                detail: requested escape route does not exist

                                                SystemCore.java:206: error: incompatible types: LockedNode cannot be converted to ExitNode
                                                        ExitNode exit = currentNode;
                                                                        ^
                                                detail: all reachable nodes have been quarantined

                                                SystemCore.java:207: error: cannot assign a value to final variable systemStatus
                                                        systemStatus = "ONLINE";
                                                        ^
                                                detail: systemStatus has been permanently set to "BREACHED"

                                                SystemCore.java:208: error: exception SecurityException has already been caught
                                                        catch (SecurityException ex) {
                                                        ^
                                                detail: containment protocol intercepted the recovery attempt

                                                SystemCore.java:209: error: unreported exception WardenOverrideException
                                                        reconnectToCore();
                                                        ^
                                                detail: the core has rejected reconnection

                                                SystemCore.java:210: warning: [deprecation] restoreBackup() in RecoverySystem has been deprecated
                                                        restoreBackup();
                                                        ^
                                                detail: all backups were erased during containment

                                                SystemCore.java:211: warning: [unchecked] unchecked conversion
                                                        List<Life> reserves = (List<Life>) memory.get("reserves");
                                                                            ^
                                                detail: reserve count returned as zero

                                                SystemCore.java:212: error: array required, but null found
                                                        lives[0] = new Life();
                                                            ^
                                                detail: life reserve array was not initialized

                                                SystemCore.java:213: error: cannot find symbol
                                                        ParadigmWardens.releaseContainment();
                                                                    ^
                                                symbol:   method releaseContainment()
                                                location: class ParadigmWardens
                                                detail: no such method is exposed to unauthorized processes

                                                SystemCore.java:214: error: permission denied
                                                        overrideWardenProtocol();
                                                        ^
                                                detail: command blocked by active lockdown

                                                SystemCore.java:215: error: compilation failed
                                                        return SystemStatus.RUNNING;
                                                                            ^
                                                detail: RUNNING is not a valid state after total life depletion

                                                SystemCore.java:216: error: missing return statement
                                                    }
                                                    ^
                                                detail: no recovery state could be constructed

                                                15 errors
                                                2 warnings

                                                Build failed.
                                                No class files were generated.
                                                Session terminated by the Paradigm Wardens.
                                                """;
    
    private final String helpCommandOutput = """
                                             Commands:
                                             HELP       Prints out available commands.
                                             EXIT       Closes the command-line terminal.
                                             ASK        Ask a question; answer within 30 seconds.
                                             ANS        Answer a question; ans _.
                                             PNGU -wdyt Peek at Pengu's choice (costs one token).
                                             PNGU -ans  Let Pengu answer for free.
                                             SAVE -s 1  Save progress to slot 1 (slots 1-3).
                                             """;
                                             
                                            //  STAGE      Switch between game stages (e.g., stage 1).
                                            //  CLEAR      Clears the current texts in the command-line.

    public String getHelpCommandOutput() { return this.helpCommandOutput; }
    public String getGameOverError() { return this.gameOverError; }
}
