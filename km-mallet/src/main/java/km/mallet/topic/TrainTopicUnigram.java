package km.mallet.topic;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.InstanceList;
import km.common.Config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TrainTopicUnigram {

	public static void main(String[] args) throws IOException {
		// String path = "E:/project/kd/data/mallet/";
		String inputFile = Config.settings.getMalletSettings().getPostsMalletPath();
		int numTopics = Config.settings.getMalletSettings().getTopicCnt(); // 50
		String topicKeysFile = Config.settings.getMalletSettings().getKeysPath(); // Setting.MalletSetting.ROOT_FOLDER + numTopics + "_keys.txt";
		String docTopicsFile = Config.settings.getMalletSettings().getTopicsPath(); // Setting.MalletSetting.ROOT_FOLDER + numTopics + "_topics.txt";

		int showTopicsInterval = 100;
		int topWords = 10;
		int numIterations = 1000; // 1000
		int optimizeInterval = 10; // set as per http://mallet.cs.umass.edu/topics.php
		int optimizeBurnIn = 20;  // 200
		double docTopicsThreshold = 0; // 0.02
		int docTopicsMax = 1; // 20
		int numThreads = 10;
		boolean useSymmetricAlpha = false;
		int randomSeed = 0; // 0

		// Start a new LDA topic model
		ParallelTopicModel topicModel = null;

		InstanceList training = InstanceList.load(new File(inputFile));
		System.out.println("Data loaded.");

		if (training.size() > 0 && training.get(0) != null) {
			Object data = training.get(0).getData();
			if (!(data instanceof FeatureSequence)) {
				System.err.println("Topic modeling currently only supports feature sequences: use --keep-sequence option when importing data.");
				System.exit(1);
			}
		}

		double alpha = 50;
		double beta = 0.01;
		topicModel = new ParallelTopicModel(numTopics, alpha, beta);
		if (randomSeed != 0) {
			topicModel.setRandomSeed(randomSeed);
		}

		topicModel.addInstances(training);

		topicModel.setTopicDisplay(showTopicsInterval, topWords);
		topicModel.setNumIterations(numIterations);
		topicModel.setOptimizeInterval(optimizeInterval);
		topicModel.setBurninPeriod(optimizeBurnIn);
		topicModel.setSymmetricAlpha(useSymmetricAlpha);
		topicModel.setNumThreads(numThreads);

		topicModel.estimate();

		topicModel.printTopWords(new File(topicKeysFile), topWords, false);

		PrintWriter out = new PrintWriter(new FileWriter((new File(docTopicsFile))));
		topicModel.printDocumentTopics(out, docTopicsThreshold, docTopicsMax);
		out.close();
	}
}
