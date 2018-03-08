import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ReadFileUtil {

	public static void main(String[] args) throws IOException {

		ReadFileUtil readFileUtil = new ReadFileUtil();
//		readFileUtil.readFileFromPDF("Statement_2017-12-08.pdf");
		readFileUtil.readFileFromPDF("Statement_2018-01-11.pdf");

	}

	private void readFileFromPDF(String fileName) throws IOException {
		//Get file from resources folder
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());

		PDDocument document = PDDocument.load(file);

		PDFTextStripperByArea textStripper = new PDFTextStripperByArea();
		Rectangle2D rect = new java.awt.geom.Rectangle2D.Float(10, 80, 400, 750);


		textStripper.addRegion("region", rect);

		Iterator<PDPage> it = document.getPages().iterator();

		List<String> allLines = new ArrayList<>();
		while (it.hasNext()){
			PDPage docPage = it.next();

			textStripper.extractRegions(docPage);

			String textForRegion = textStripper.getTextForRegion("region");

			List<String> singleLines = getStrings(textForRegion);

			allLines.addAll(singleLines);
		}

		System.out.println(allLines);

		document.close();

	}

	private List<String> getStrings(String textForRegion) {
		List<String> lines = Arrays.asList(textForRegion.split("\n"));

//		3,000.00
		String moneyReg = ".*\\s[0-9]{1,3}(?:,?[0-9]{3})*\\.[0-9]{2}";

		String pureMoneyReg = "^[0-9]{1,3}(?:,?[0-9]{3})*\\.[0-9]{2}$";

		Pattern moneyPattern = Pattern.compile(moneyReg);
		Pattern pureMoneyPattern = Pattern.compile(pureMoneyReg);

		List<String> multiLines = new ArrayList<>();
		for (int i=0; i<lines.size();i++) {
			if(!moneyPattern.matcher(lines.get(i)).matches()) {
				//TODO only can handle 2 lines, bug in 3 lines
				if(i+2>lines.size()-1) break;
				if (pureMoneyPattern.matcher(lines.get(i+2)).matches()) {
					String newLine = lines.get(i)+" "+lines.get(i+1)+" "+lines.get(i+2)+"\n";
					multiLines.add(newLine);
					i+=2;
				}
			}
		}

		List<String> singleLines = lines.stream().filter(line->moneyPattern.matcher(line).matches()).map(l->l+"\n").collect(Collectors.toList());

		singleLines.addAll(multiLines);
		return singleLines;
	}
}
