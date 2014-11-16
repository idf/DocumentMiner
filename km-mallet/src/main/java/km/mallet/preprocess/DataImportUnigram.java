package km.mallet.preprocess;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.util.CharSequenceLexer;
import km.common.Setting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DataImportUnigram {
	public static String defaultLineRegex = "^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$";
	public static String defaultTokenRegex = "\\p{L}[\\p{L}\\p{P}]+\\p{L}";

	public static void main(String[] args) throws IOException {
		File inputFile = new File(Setting.MalletSetting.POSTS_PATH); // "E:/project/kd/data/mallet/posts.csv"
		// File extraStopwordFile = new File(Setting.MalletSetting.ROOT_FOLDER, "extra_stopwords.txt");
        File extraStopwordFile = new File(Setting.MalletSetting.ROOT_FOLDER, "stoplist/en.txt");
		File outputFile = new File(Setting.MalletSetting.POSTS_MALLET_PATH);

		Pipe instancePipe;

		// Build a new pipe
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Convert the "target" object into a numeric index into a LabelAlphabet.
		// If the label field is not used, adding this
		// pipe will cause "Alphabets don't match" exceptions.
		pipeList.add(new Target2Label());

		// Tokenize the input: first compile the tokenization pattern
		Pattern tokenPattern = null;

		// try to compile the regular expression pattern.
		String regex = CharSequenceLexer.LEX_ALPHA.toString();
		try {
			tokenPattern = Pattern.compile(defaultTokenRegex);
		} catch (PatternSyntaxException pse) {
			throw new IllegalArgumentException("The token regular expression (" + regex + ") was invalid: " + pse.getMessage());
		}
		
		// String replacements
		pipeList.add(new CharSequenceLowercase());

		// Add the tokenizer
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));

		// Normalize the input as necessary
		// Stopword removal. so use the default built-in English list
		TokenSequenceRemoveStopwords stopwordFilter = new TokenSequenceRemoveStopwords(false, false);
		stopwordFilter.addStopWords(extraStopwordFile);

		pipeList.add(stopwordFilter);

		// Output is unigram feature sequences
		pipeList.add(new TokenSequence2FeatureSequence());

		instancePipe = new SerialPipes(pipeList);

		// Create the instance list and open the input file
		InstanceList instances = new InstanceList(instancePipe);
		Reader fileReader = new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8);

		// Read instances from the file
		int dataOption = 3;
		int labelOption = 2;
		int nameOption = 1;
		instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile(defaultLineRegex), dataOption, labelOption, nameOption));

		// Save instances to output file
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile));
		oos.writeObject(instances);
		oos.close();
	}
}
