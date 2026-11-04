package ru.mephi.promoit.urlshortener.cli.browser;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class DesktopBrowserLauncher implements BrowserLauncher {

    @Override
    public void open(URI uri) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop API is not supported.");
        }

        Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Browser opening is not supported.");
        }

        desktop.browse(uri);
    }
}
