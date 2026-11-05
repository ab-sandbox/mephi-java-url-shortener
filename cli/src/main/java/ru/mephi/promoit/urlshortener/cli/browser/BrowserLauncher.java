package ru.mephi.promoit.urlshortener.cli.browser;

import java.io.IOException;
import java.net.URI;

public interface BrowserLauncher {

    void open(URI uri) throws IOException;
}
