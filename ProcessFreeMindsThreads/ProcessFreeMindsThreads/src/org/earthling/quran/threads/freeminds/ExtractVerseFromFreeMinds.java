package org.earthling.quran.threads.freeminds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jqurantree.orthography.Document;
import org.jqurantree.orthography.Location;
import org.jqurantree.orthography.Verse;
import org.w3c.dom.Node;

import se.fishtank.css.selectors.NodeSelectorException;
import se.fishtank.css.selectors.dom.DOMNodeSelector;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ExtractVerseFromFreeMinds {

	private Map<String, HashMap<String, Set<Post>>> verseMap = new HashMap<String, HashMap<String, Set<Post>>>();

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		new ExtractVerseFromFreeMinds().process(OutputType.HTMLIMBEDED);
	}

	private void process(OutputType outputType) throws FileNotFoundException {
		File dir = new File(ApplicationProperties.get("dir"));
		String threadLink = ApplicationProperties.get("threadLink");
		String formattedChapterVerseLink = ApplicationProperties
				.get("formattedChapterVerseLink");
		String formattedChapterVerseImageLink = ApplicationProperties
				.get("formattedChapterVerseImageLink");

		if (dir.exists()) {
			for (File f : dir.listFiles()) {
				try {
					if (f.isFile())
						process(f);
				} catch (FailingHttpStatusCodeException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ArrayList<String> sortedList = new ArrayList<String>(
					verseMap.keySet());
			Collections.sort(sortedList);
			FileOutputStream fos = new FileOutputStream("output.html");
			PrintStream out = new PrintStream(fos);
			if (outputType == OutputType.HTML
					|| outputType == OutputType.HTMLIMBEDED) {
				out.println("<html>");
				out.println("<body>");
			}
			for (String key : sortedList) {
				try {
					StringTokenizer s = new StringTokenizer(key, ";:");
					Integer chapter = Integer.valueOf(s.nextToken().trim());
					Integer verse = Integer.valueOf(s.nextToken().trim());
					if (outputType == OutputType.TEXT) {
						out.println("");
						out.printf("Verse %d:%d", chapter, verse);
						out.println("");
						out.print("References:");
						out.println("");
					} else if (outputType == OutputType.HTML) {
						out.println("<br/>");
						out.printf("Verse:<a href='"
								+ formattedChapterVerseLink + "'>%s</a>",
								chapter, verse, key);
						out.println("<br/>");
						out.print("References: ");
						out.println("<br/>");
					} else if (outputType == OutputType.HTMLIMBEDED) {
						out.println("<br/>");
						out.printf("Verse:<a href='"
								+ formattedChapterVerseLink + "'>%s</a>",
								chapter, verse, key);
						out.println("<br/>");
						out.printf("<img src='"
								+ formattedChapterVerseImageLink + "'/>",
								chapter, verse);
						out.println("<br/>");
						out.print("References: ");
						out.println("<br/>");
					}
					HashMap<String, Set<Post>> hashMap = verseMap.get(key);
					int cPk = 0;
					ArrayList<String> sortedName = new ArrayList<String>(
							hashMap.keySet());
					Collections.sort(sortedName);
					for (String pk : sortedName) {
						if (cPk++ > 0) {
							if (outputType == OutputType.TEXT) {
								out.println("; ");
							} else if (outputType == OutputType.HTML
									|| outputType == OutputType.HTMLIMBEDED) {
								out.println("<br/>");
							}
						}
						if (outputType == OutputType.HTML
								|| outputType == OutputType.TEXT
								|| outputType == OutputType.HTMLIMBEDED) {
							out.print(pk + " => ");
						}
						int cP = 0;
						ArrayList<Post> sortedPost = new ArrayList<Post>(
								hashMap.get(pk));
						Collections.sort(sortedPost);
						for (Post p : sortedPost) {
							if (cP++ > 0)
								out.print(" , ");

							if (outputType == OutputType.TEXT) {
								out.print(p.getPostNo());
							} else if (outputType == OutputType.HTML
									|| outputType == OutputType.HTMLIMBEDED) {
								out.printf("<a href='" + threadLink
										+ "%d'>%d</a>", p.getPostNo(),
										p.getPostNo());
							}
						}
					}
					if (outputType == OutputType.TEXT) {
						out.println();
					} else if (outputType == OutputType.HTML
							|| outputType == OutputType.HTMLIMBEDED) {
						out.println("<br/>");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (outputType == OutputType.HTML
						|| outputType == OutputType.HTMLIMBEDED) {
					out.println("</body>");
					out.println("</html>");
				}
			}
		}
	}

	private void process(File f) throws FailingHttpStatusCodeException,
			MalformedURLException, IOException {
		final WebClient webClient = new WebClient();
		webClient.setJavaScriptEnabled(false);
		final HtmlPage page = webClient
				.getPage("file://" + f.getAbsolutePath());
		// assertEquals("HtmlUnit - Welcome to HtmlUnit", page.getTitleText());

		final DOMNodeSelector cssSelector = new DOMNodeSelector(
				page.getDocumentElement());
		Set elements;
		try {
			elements = cssSelector
					.querySelectorAll("form[id = 'quickModForm']>div");
			// System.out.println(elements.size());
			Iterator iterator = elements.iterator();
			Pattern p = Pattern
					.compile("("
							+ "(\\d{1,2})\\s*:\\s*(\\d{1,2})\\s*:\\s*(\\d{1,2}) (AM|PM)|"
							+ "((\\d{1,4})\\s*:\\s*(\\d{1,4})\\s*-\\s*((\\d{1,4})\\s*:\\s*(\\d{1,4})))|"
							+ "((\\d{1,4})\\s*:\\s*(\\d{1,4})\\s*-\\s*\\d{1,4})|"
							+ "\\d{1,4}\\s*:\\s*\\d{1,4}" + ")");
			Pattern pTime = Pattern
					.compile("("
							+ "(\\d{1,2})\\s*:\\s*(\\d{1,2})\\s*:\\s*(\\d{1,2}) (AM|PM)"
							+ ")");
			Pattern pDelta1 = Pattern.compile("("
					+ "((\\d{1,4})\\s*:\\s*(\\d{1,4})\\s*-\\s*\\d{1,4})" + ")");
			Pattern pDelta2 = Pattern
					.compile("("
							+ "((\\d{1,4})\\s*:\\s*(\\d{1,4})\\s*-\\s*((\\d{1,4})\\s*:\\s*(\\d{1,4})))"
							+ ")");

			while (iterator.hasNext()) {
				final Node node = (Node) iterator.next();

				int no = 0;
				String name = "";
				System.out.println(node.toString());
				final DOMNodeSelector selDiv = new DOMNodeSelector(node);
				Set posterSet = selDiv
						.querySelectorAll("div[class='poster'] a");
				if (posterSet.size() > 0) {
					Node poster = (Node) posterSet.iterator().next();
					name = poster.getTextContent();
					// System.out.println("name:"+poster.getTextContent());
				}
				Set postNoSet = selDiv
						.querySelectorAll("div[class='postarea'] div[class='keyinfo'] strong");
				if (postNoSet.size() > 0) {
					Node postNo = (Node) postNoSet.iterator().next();
					String pNo = postNo.getTextContent();
					// System.out.println("post no:" + pNo);
					if (pNo.startsWith("Reply #")) {
						no = Integer
								.valueOf(pNo.substring(7, pNo.length() - 4));
					}
					System.out.println("post no:" + no);
				}
				Post post = Post.getInstance(name, no);

				Set textSet = selDiv
						.querySelectorAll("div[class='postarea'] div[class='post']");
				if (posterSet.size() > 0) {
					Node text = (Node) textSet.iterator().next();
					// System.out.println("post:"+ text.getTextContent());
					Matcher m = p.matcher(text.getTextContent());
					while (m.find()) {
						String foundText = m.group();
						// System.out.println(foundText);
						Matcher mTime = pTime.matcher(foundText);
						Matcher mDelta1 = pDelta1.matcher(foundText);
						Matcher mDelta2 = pDelta2.matcher(foundText);
						if (mTime.find()) {
							continue;
						} else if (mDelta2.find()) {
							StringTokenizer s = new StringTokenizer(foundText,
									":-");
							Integer chapter = Integer.valueOf(s.nextToken()
									.trim());
							Integer verseBegin = Integer.valueOf(s.nextToken()
									.trim());
							String chapter2 = s.nextToken().trim();
							Integer verseEnd = Integer.valueOf(s.nextToken()
									.trim());
							for (int i = Integer.valueOf(verseBegin); i <= Integer
									.valueOf(verseEnd); i++) {
								// System.out.println(chapter+":"+i);
								postToMap(verseMap, chapter, i, post);
							}
						} else if (mDelta1.find()) {
							StringTokenizer s = new StringTokenizer(foundText,
									":-");
							Integer chapter = Integer.valueOf(s.nextToken()
									.trim());
							Integer verseBegin = Integer.valueOf(s.nextToken()
									.trim());
							Integer verseEnd = Integer.valueOf(s.nextToken()
									.trim());
							for (int i = verseBegin; i <= verseEnd; i++) {
								// System.out.println(chapter+":"+i);
								postToMap(verseMap, chapter, i, post);
							}
						} else {
							StringTokenizer s = new StringTokenizer(foundText,
									":-");
							String chapter = s.nextToken().trim();
							String verse = s.nextToken().trim();
							postToMap(verseMap, Integer.valueOf(chapter),
									Integer.valueOf(verse), post);
						}
					}
				}
			}
		} catch (NodeSelectorException e) {
			e.printStackTrace();
		}
		final String pageAsXml = page.asXml();
		// assertTrue(pageAsXml.contains("<body class=\"composite\">"));

		final String pageAsText = page.asText();
		// assertTrue(pageAsText.contains("Support for the HTTP and HTTPS protocols"));

		webClient.closeAllWindows();
	}

	private void postToMap(Map<String, HashMap<String, Set<Post>>> verseMap,
			Integer chapterI, Integer verseI, Post post) {
		// Get verse (27:30) by location.
		Location location = null;
		Verse v = null;
		// String chapter = String.format("%3d", chapterI);
		// String verse = String.format("%3d", Integer.valueOf(verseI));
		try {
			location = new Location(chapterI, verseI);
			v = Document.getVerse(location);
		} catch (Exception e) {
		}
		if (location != null && v != null) {
			String key = String.format("%03d:%03d", chapterI, verseI);
			HashMap<String, Set<Post>> hashMap = verseMap.get(key);
			if (hashMap == null) {
				hashMap = new HashMap<String, Set<Post>>();
				verseMap.put(key, hashMap);
			}
			Set<Post> postsSet = hashMap.get(post.getName());
			if (postsSet == null) {
				postsSet = new HashSet<Post>();
				hashMap.put(post.getName(), postsSet);
			}
			postsSet.add(post);
		}
	}

}
