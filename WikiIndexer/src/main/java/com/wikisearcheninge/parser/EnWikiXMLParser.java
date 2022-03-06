package com.wikisearcheninge.parser;

import com.wikisearcheninge.exceptions.NoMoreDataException;
import com.wikisearcheninge.wikimedia.MultiStreamBZip2InputStream;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnWikiXMLParser extends DefaultHandler implements Runnable {
    private Thread t;
    private boolean threadDone;
    private boolean stopped = false;
    private String[] tuple;
    private NoMoreDataException nmde;
    private StringBuilder contents = new StringBuilder();
    private String title;
    private String body;
    private String time;
    private String id;
    private InputStream is;
    private Path file;

    private static final Map<String, Integer> ELEMENTS = new HashMap<>();
    public static final int TITLE = 0;
    public static final int DATE = TITLE + 1;
    public static final int BODY = DATE + 1;
    public static final int ID = BODY + 1;
    public static final int LENGTH = ID + 1;
    // LENGTH is used as the size of the tuple, so whatever constants we need that
    // should not be part of the tuple, we should define them after LENGTH.
    private static final int PAGE = LENGTH + 1;

    private static final String[] months = {"JAN", "FEB", "MAR", "APR",
            "MAY", "JUN", "JUL", "AUG",
            "SEP", "OCT", "NOV", "DEC"};

    static {
        ELEMENTS.put("page", Integer.valueOf(PAGE));
        ELEMENTS.put("text", Integer.valueOf(BODY));
        ELEMENTS.put("timestamp", Integer.valueOf(DATE));
        ELEMENTS.put("title", Integer.valueOf(TITLE));
        ELEMENTS.put("id", Integer.valueOf(ID));
    }

    public EnWikiXMLParser(String dumpFilePath){
        file = Paths.get(dumpFilePath).toAbsolutePath();
    }

    /**
     * Returns the type of the element if defined, otherwise returns -1. This
     * method is useful in startElement and endElement, by not needing to compare
     * the element qualified name over and over.
     */
    private final static int getElementType(String elem) {
        Integer val = ELEMENTS.get(elem);
        return val == null ? -1 : val.intValue();
    }


    public String[] next() throws NoMoreDataException {
        if (t == null) {
            threadDone = false;
            t = new Thread(this);
            t.setDaemon(true);
            t.start();
        }
        String[] result;
        synchronized (this) {
            while (tuple == null && nmde == null && !threadDone && !stopped) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
            if (tuple != null) {
                result = tuple;
                tuple = null;
                notify();
                return result;
            }
            if (nmde != null) {
                // Set to null so we will re-start thread in case
                // we are re-used:
                t = null;
                throw nmde;
            }
            // The thread has exited yet did not hit end of
            // data, so this means it hit an exception.  We
            // throw NoMorDataException here to force
            // benchmark to stop the current alg:
            throw new NoMoreDataException();
        }
    }

    String time(String original) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(original.substring(8, 10));
        buffer.append('-');
        buffer.append(months[Integer.valueOf(original.substring(5, 7)).intValue() - 1]);
        buffer.append('-');
        buffer.append(original.substring(0, 4));
        buffer.append(' ');
        buffer.append(original.substring(11, 19));
        buffer.append(".000");

        return buffer.toString();
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        contents.append(ch, start, length);
    }

    @Override
    public void endDocument(){
        synchronized (this) {
            threadDone = true;
            stopped = true;
            tuple = null;
            notify();
        }
    }
    @Override
    public void endElement(String namespace, String simple, String qualified)
            throws SAXException {
        int elemType = getElementType(qualified);
        switch (elemType) {
            case PAGE:
                // the body must be null and we either are keeping image docs or the
                // title does not start with Image:
                if (body != null && (!title.startsWith("Image:"))) {
                    String[] tmpTuple = new String[LENGTH];
                    tmpTuple[TITLE] = title.replace('\t', ' ');
                    tmpTuple[DATE] = time.replace('\t', ' ');
                    tmpTuple[BODY] = body.replaceAll("[\t\n]", " ");
                    tmpTuple[ID] = id;
                    synchronized (this) {
                        while (tuple != null && !stopped) {
                            try {
                                wait();
                            } catch (InterruptedException ie) {
                                throw new RuntimeException(ie);
                            }
                        }
                        tuple = tmpTuple;
                        notify();
                    }
                }
                break;
            case BODY:
                body = contents.toString();
                //workaround that startswith doesn't have an ignore case option, get at least 20 chars.
                String startsWith = body.substring(0, Math.min(10, contents.length())).toLowerCase(Locale.ROOT);
                if (startsWith.startsWith("#redirect")) {
                    body = null;
                }
                break;
            case DATE:
                time = time(contents.toString());
                break;
            case TITLE:
                title = contents.toString();
                break;
            case ID:
                //the doc id is the first one in the page.  All other ids after that one can be ignored according to the schema
                if (id == null) {
                    id = contents.toString();
                }
                break;
            default:
                // this element should be discarded.
        }
    }

    public Reader getDecodingReader(InputStream stream, Charset charSet) {
        final CharsetDecoder charSetDecoder = charSet.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return new BufferedReader(new InputStreamReader(new MultiStreamBZip2InputStream(stream), charSetDecoder));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {

        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            reader.setErrorHandler(this);
            while (!stopped) {
                final InputStream localFileIS = is;
                if (localFileIS != null) { // null means fileIS was closed on us
                    try {
                        // To work around a bug in XERCES (XERCESJ-1257), we assume the XML is always UTF8, so we simply provide reader.
                        reader.parse(new InputSource(getDecodingReader(localFileIS, StandardCharsets.UTF_8)));
                    } catch (IOException ioe) {
                        synchronized (EnWikiXMLParser.this) {
                            if (localFileIS != is) {
                                // fileIS was closed on us, so, just fall through
                            } else
                                // Exception is real
                                throw ioe;
                        }
                    }
                }
                synchronized (this) {
                    if (stopped) {
                        nmde = new NoMoreDataException();
                        notify();
                        return;
                    } else if (localFileIS == is) {
                        // If file is not already re-opened then re-open it now
                        is = openInputStream();
                    }
                }
            }
        } catch (SAXException | IOException sae) {
            throw new RuntimeException(sae);
        } finally {
            synchronized (this) {
                threadDone = true;
                notify();
            }
        }
    }

    @Override
    public void startElement(String namespace, String simple, String qualified,
                             Attributes attributes) {
        int elemType = getElementType(qualified);
        switch (elemType) {
            case PAGE:
                title = null;
                body = null;
                time = null;
                id = null;
                break;
            // intentional fall-through.
            case BODY:
            case DATE:
            case TITLE:
            case ID:
                contents.setLength(0);
                break;
            default:
                // this element should be discarded.
        }
    }

    private void stop() {
        synchronized (this) {
            stopped = true;
            if (tuple != null) {
                tuple = null;
                notify();
            }
        }
    }

    /**
     * Open the input stream.
     */
    protected InputStream openInputStream() throws IOException {
        return new FileInputStream(String.valueOf(file));
    }

}

