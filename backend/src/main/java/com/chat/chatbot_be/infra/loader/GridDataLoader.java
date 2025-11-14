package com.chat.chatbot_be.infra.loader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface GridDataLoader {
    List<List<String>> load(File file) throws IOException;
}