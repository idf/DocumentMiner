package km.mallet.preprocess;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.util.CharSequenceLexer;
import km.common.Settings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class DateImportBigram {
	public static String defaultLineRegex = "^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$";
	
	public static void main(String[] args) throws IOException {
		File inputFile = new File(Settings.MalletSettings.POSTS_PATH); // "E:/project/kd/data/mallet/posts.csv"
		File outputFile = new File(Settings.MalletSettings.ROOT_FOLDER, "posts_bigram.mallet"); // "E:/project/kd/data/mallet/posts_bigram.mallet"

		Pipe instancePipe;

		// Build a new pipe
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Convert the "target" object into a numeric index
		// into a LabelAlphabet.

		// If the label field is not used, adding this
		// pipe will cause "Alphabets don't match" exceptions.
		pipeList.add(new Target2Label());

		// Tokenize the input: first compile the tokenization pattern
		// We do not want to record bigrams across punctuation,
		// so we need to keep non-word tokens.
		Pattern tokenPattern = CharSequenceLexer.LEX_NONWHITESPACE_CLASSES;

		// String replacements
		pipeList.add(new CharSequenceLowercase());
		
		// Add the tokenizer
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));

		// Normalize the input as necessary
		
		// Remove non-word tokens, but record the fact that they were there.
		pipeList.add(new TokenSequenceRemoveNonAlpha(true));

		// Stopword removal. use the default built-in English list
		TokenSequenceRemoveStopwords stopwordFilter = new TokenSequenceRemoveStopwords(false, true);

		pipeList.add(stopwordFilter);
		
		// Convert tokens to numeric indices into the Alphabet

		// Output is feature sequences with bigram features
		pipeList.add(new TokenSequence2FeatureSequenceWithBigrams());

		instancePipe = new SerialPipes(pipeList);

		// Create the instance list and open the input file
		InstanceList instances = new InstanceList(instancePipe);
		Reader fileReader = new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8);

		// Read instances from the file
		int dataOption = 3;
		int labelOption = 2;
		int nameOption = 1;
		Pattern p = Pattern.compile(defaultLineRegex);
		CsvIterator ci = new CsvIterator(fileReader, p, dataOption, labelOption, nameOption);
		instances.addThruPipe(ci);

		// Save instances to output file
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
		oos.writeObject(instances);
		oos.close();
	}
}
