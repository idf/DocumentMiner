package km.mallet.topic;

import cc.mallet.topics.TopicalNGrams;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;
import km.common.Config;

import java.io.*;

public class TrainTopicBigram {

	public static void main(String[] args) throws IOException {
		// String path = "E:/project/kd/data/mallet/";
		String inputFile = Config.settings.getMalletSettings().getRootFolder() + "posts_bigram.mallet";
		int numTopics = Config.settings.getMalletSettings().getTopicCnt();	// 50
		String topicKeysFile = Config.settings.getMalletSettings().getRootFolder() + "phrase_" + numTopics + "_keys.txt";
		String docTopicsFile = Config.settings.getMalletSettings().getRootFolder() + "phrase_" + numTopics + "_topics.txt";
		
		int showTopicsInterval = 100;
		int topWords = 10;
		int numIterations = 1000;	// 1000
		double docTopicsThreshold = 0;	// 0.02
		int docTopicsMax = 1;	// 20
		int randomSeed = 1;	// 0
		
		InstanceList ilist = InstanceList.load(new File(inputFile));
		System.out.println("Data loaded.");
		
		double alpha = 50;
		double beta = 0.01;
		double gamma = 0.01;
		double delta = 0.03;
		double delta1 = 0.2;
		double delta2 = 1000.0;
		TopicalNGrams tng = new TopicalNGrams(numTopics, alpha, beta, gamma, delta, delta1, delta2);
		int outputModelInterval = 0;
		String outputModelFilename = null;
		tng.estimate(ilist, numIterations, showTopicsInterval, outputModelInterval, outputModelFilename, randomSeed == 0 ? new Randoms() : new Randoms(randomSeed));
		
		PrintStream ps = new PrintStream(new File(topicKeysFile));
		tng.printTopWords(ps, topWords, true);
		ps.close();

		PrintWriter out = new PrintWriter(new FileWriter(new File(docTopicsFile)));
		tng.printDocumentTopics(out, docTopicsThreshold, docTopicsMax);
		out.close();
	}
}
