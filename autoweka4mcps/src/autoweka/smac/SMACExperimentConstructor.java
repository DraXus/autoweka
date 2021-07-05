package autoweka.smac;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import autoweka.Conditional;
import autoweka.ExperimentConstructor;
import autoweka.Parameter;
import autoweka.ParameterConditionalGroup;
import autoweka.Util;

public class SMACExperimentConstructor extends ExperimentConstructor
{

    public void prepareExperiment(String path)
    {
        try
        {
            //Print out the param file
            printParamFile(new PrintStream(new java.io.File(path + "autoweka.params")));

            //Write out the instance file
            printInstanceFile(new PrintStream(new java.io.File(path + "autoweka.instances")));
            printTestInstanceFile(new PrintStream(new java.io.File(path + "autoweka.test.instances")));
            printFeatureFile(new PrintStream(new java.io.File(path + "autoweka.features")));

            //Write out the scenario file
            printScenarioFile(new PrintStream(new java.io.File(path + "autoweka.scenario")));


            autoweka.Util.makePath(path + "out");
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to prepare the experiment", e);
        }
    }

    public String getTrajectoryParserClassName()
    {
        return "autoweka.smac.SMACTrajectoryParser";
    }

    @Override
    public List<String> getCallString(String experimentPath)
    {
        //Make sure that the properties we have tell us where the executable for smac lives
        if(mProperties.getProperty("smacexecutable") == null)
            throw new RuntimeException("The 'smacexecutable' property was not defined");

        Properties props = autoweka.Util.parsePropertyString(mExperiment.extraPropsString);

        String execExtension = "";
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0){
            execExtension = ".bat";
        }

        File f = new File(Util.expandPath(mProperties.getProperty("smacexecutable") + execExtension));
        if(!f.exists())
            throw new RuntimeException("Could not find SMAC executable '" + f.getAbsoluteFile() + "'");

        List<String> args = new ArrayList<String>();
        args.add(mProperties.getProperty("smacexecutable") + execExtension);
        args.add("--seed");
        args.add("{SEED}");
        args.add("--scenarioFile");
        args.add("autoweka.scenario");
        args.add("--logModel");
        args.add("false");
        args.add("--adaptiveCapping");
        args.add("false");
        args.add("--runGroupName");
        args.add("autoweka");
        args.add("--terminate-on-delete");
        args.add(experimentPath + File.separator + "out" + File.separator + "runstamps" + File.separator + "{SEED}.stamp");
        args.add("--kill-runs-on-file-delete");
        args.add(experimentPath + File.separator + "out" + File.separator + "runstamps" + File.separator + "{SEED}.stamp");

        if(props.containsKey("deterministicInstanceOrdering"))
        {
            //throw new RuntimeException("This option only works on a hacked up version of SMAC");
            args.add("--deterministicInstanceOrdering");
            args.add(props.getProperty("deterministicInstanceOrdering"));
        }
        
        if(props.containsKey("initialIncumbent"))
        {
            args.add("--initialIncumbent");
            args.add(props.getProperty("initialIncumbent"));
        }
        
        if(props.containsKey("initialIncumbentRuns"))
        {
            args.add("--initialIncumbentRuns");
            args.add(props.getProperty("initialIncumbentRuns"));
        }

        if(props.containsKey("initialN"))
        {
            args.add("--initialN");
            args.add(props.getProperty("initialN"));
        }
        
        if(props.containsKey("initialChallenge"))
        {
            args.add("--initialChallenge");
            args.add(props.getProperty("initialChallenge"));
        }

        if(props.containsKey("stateSerializer"))
        {
            args.add("--stateSerializer");
            args.add(props.getProperty("stateSerializer"));
        }
        
        if(props.containsKey("acq-func"))
        {
            args.add("--acq-func");
            args.add(props.getProperty("acq-func"));
        }
        else
        {
            args.add("--acq-func");
            args.add("EI");
        }

        if(props.containsKey("executionMode"))
        {
            args.add("--executionMode");
            args.add(props.getProperty("executionMode"));
        }
        
        if(props.containsKey("intensificationPercentage"))
        {
            args.add("--intensification-percentage");
            args.add(props.getProperty("intensificationPercentage")); // should be between 0 and 1, def: 0.5
        }
        
//        if(props.containsKey("number-init-configs"))
//        {
//            args.add("--number-init-configs");
//            args.add(String.valueOf(mExperiment.numberOfInitConfigs));
//        }
        
        // Showing the real calls
        args.add("--cli-log-all-call-strings");
        args.add("true");

        // Clean old states on success
        args.add("--clean-old-state-on-success");
        args.add("true");
        
        // Avoid to abort if the first run crash
        args.add("--abort-on-first-run-crash");
        args.add("false");

        return args;
    }

    public String getType()
    {
        return "SMAC";
    }

    public void printInstanceFile(PrintStream out)
    {
        Properties props = autoweka.Util.parsePropertyString(mExperiment.extraPropsString);
        String instancesOverride = props.getProperty("instancesOverride", null);
        if(instancesOverride != null)
        {
            out.println(instancesOverride); 
        }
        else
        {
            List<String> instanceStrings = mInstanceGenerator.getAllInstanceStrings(mExperiment.instanceGeneratorArgs);
            for(String s:instanceStrings)
            {
                out.println(s);
            }
        }
    }

    public void printFeatureFile(PrintStream out)
    {
        Set<String> featureNamesSet = new HashSet<String>();

        Map<String, Map<String, String>> features = mInstanceGenerator.getAllInstanceFeatures(mExperiment.instanceGeneratorArgs);
        for(String inst:features.keySet())
        {
            featureNamesSet.addAll(features.get(inst).keySet());
        }
        String[] featureNames = featureNamesSet.toArray(new String[]{});

        //Write out the main row
        out.print("instance");
        for(String feat: featureNames)
            out.print("," +feat);
        out.println();

        //Write out each feature
        for(String inst:features.keySet())
        {
            out.print(inst);
            for(String feat: featureNames)
                out.print("," + features.get(inst).get(feat));
            out.println();
        }
    }

    public void printTestInstanceFile(PrintStream out)
    {
        out.println("default");
    }

    public void printScenarioFile(PrintStream out)
    {
        String extraProps = "";
        if(mExperiment.extraPropsString != null && mExperiment.extraPropsString.length() > 0)
            extraProps = " -prop " + mExperiment.extraPropsString;

        Properties props = autoweka.Util.parsePropertyString(mExperiment.extraPropsString);
        String wrapper = props.getProperty("wrapper", "autoweka.smac.SMACWrapper");

        out.println("algo = \"" + autoweka.Util.getJavaExecutable() + "\" -Xmx" + mExperiment.memory + " -cp \"" + autoweka.Util.getAbsoluteClasspath() + "\" " + wrapper + " -prop " + getWrapperPropString() + extraProps + " -wrapper");
        out.println("execdir = ./");
        out.println("deterministic = 1");
        out.println("run_obj = quality");
        out.println("overall_obj = mean");
        out.println("cutoff_time = " + (int)mExperiment.trainTimeout);
        out.println("tunerTimeout = " + (int)mExperiment.tunerTimeout);
        out.println("outdir = out");
        out.println("paramfile = autoweka.params");
        out.println("instance_file = autoweka.instances");
        out.println("test_instance_file = autoweka.test.instances");
        if(!mInstanceGenerator.getAllInstanceFeatures(mExperiment.instanceGeneratorArgs).isEmpty() && mProperties.get("instancesOverride") != null)
            out.println("feature_file = autoweka.features");
    }

    public void printParamFile(PrintStream out)
    {
        ParameterConditionalGroup paramGroup = generateAlgorithmParameterConditionalGroupForDAG();
        List<String> parameters = new ArrayList<String>();
        List<String> conditionals = new ArrayList<String>();

        for(Parameter param: paramGroup.getParameters()){
            parameters.add(param.toString());
            for(Conditional cond: paramGroup.getConditionalsForParameter(param)){
                conditionals.add(cond.toString());
            }
        }

        //Sort them for sanity
        Collections.sort(parameters);
        Collections.sort(conditionals);

        //Dump 'em
        for(String param: parameters)
        {
            out.println(param);
        }
        out.println("Conditionals:");
        for(String cond: conditionals)
        {
            out.println(cond);
        }
    }
}