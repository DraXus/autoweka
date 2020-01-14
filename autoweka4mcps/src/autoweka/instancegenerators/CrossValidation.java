package autoweka.instancegenerators;

import autoweka.InstanceGenerator;
import autoweka.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import weka.core.Instances;

/** 
 * Splits the training data up into CV folds with a given seed.
 *
 * instanceGeneratorArguments: A property string with:
 *   seed - The seed to use for splitting up the training data
 *   numFolds - The number of folds to make
 *  
 * instance string format:
 *   seed - The seed to use for splitting up the training data
 *   numFolds - The number of folds to make
 *   fold - the index of the current fold   
 */ 
public class CrossValidation extends InstanceGenerator
{
    public CrossValidation(InstanceGenerator generator)
    {
        super(generator);
    }

    public CrossValidation(String instanceFileName)
    {
        super(instanceFileName);
    }

    public CrossValidation(Instances training, Instances testing)
    {
        super(training, testing);
    }

    public Instances _getTrainingFromParams(String paramString)
    {
        return getInstances(true, getTraining(), Util.parsePropertyString(paramString));
    }

    public Instances _getTestingFromParams(String paramString)
    {
        return getInstances(false, getTraining(), Util.parsePropertyString(paramString));
    }

    protected Instances getInstances(boolean trainingFold, Instances instances, Properties params)
    {
        int seed = Integer.parseInt(params.getProperty("seed", "0"));
        int numFolds = Integer.parseInt(params.getProperty("numFolds", "-1"));
        int currentFold = Integer.parseInt(params.getProperty("fold", "-1"));

        if(numFolds <= 0)
            throw new RuntimeException("numFolds must be set to something > 0");

        if(currentFold < 0|| numFolds <= currentFold)
            throw new RuntimeException("fold must be set to something in [0," + numFolds + "]");

        Random rand = new Random(seed);
        Instances randData = getTraining();
        randData.randomize(rand);

        if(trainingFold)
            return randData.trainCV(numFolds, currentFold);
        else
            return randData.testCV(numFolds, currentFold);
    }

    public List<String> getAllInstanceStrings(String paramStr)
    {
        Properties params = Util.parsePropertyString(paramStr);

        int seed, numFolds;
        try{
            seed = Integer.parseInt(params.getProperty("seed", "0"));
        }catch(Exception e){
            throw new RuntimeException("Failed to parse seed", e);
        }
        try{
            numFolds = Integer.parseInt(params.getProperty("numFolds", "-1"));
        }catch(Exception e){
            throw new RuntimeException("Failed to parse numFolds", e);
        }

        if(numFolds <= 0)
            throw new RuntimeException("numFolds must be set to something > 0");

        List<String> instanceStrings = new ArrayList<String>(numFolds);
        for(int i = 0; i < numFolds; i++)
        {
            //Should probably change this to using a Properties object, but meh
            instanceStrings.add("seed=" + seed + ":numFolds=" + numFolds + ":fold=" + i);
        }
        return instanceStrings;
    }
}

