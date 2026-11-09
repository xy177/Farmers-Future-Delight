package xy177.farmersfuturedelight.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

final class FFDNbtPath {
    private final List<Token> tokens;

    private FFDNbtPath(List<Token> tokens) {
        this.tokens = tokens;
    }

    static FFDNbtPath parse(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException(path);
        }
        List<Token> tokens = new ArrayList<>();
        int cursor = 0;
        boolean first = true;
        while (cursor < path.length()) {
            char current = path.charAt(cursor);
            if (current == '{') {
                if (!first) {
                    throw new IllegalArgumentException(path);
                }
                ParsedFilter parsed = parseFilter(path, cursor);
                tokens.add(Token.rootFilter(parsed.filter));
                cursor = parsed.end;
            } else if (current == '[') {
                ParsedToken parsed = parseBracket(path, cursor);
                tokens.add(parsed.token);
                cursor = parsed.end;
            } else {
                String key;
                if (current == '"' || current == '\'') {
                    ParsedString parsed = parseQuoted(path, cursor);
                    key = parsed.value;
                    cursor = parsed.end;
                } else {
                    int keyStart = cursor;
                    while (cursor < path.length() && isAllowedInUnquotedName(path.charAt(cursor))) {
                        cursor++;
                    }
                    if (cursor == keyStart) {
                        throw new IllegalArgumentException(path);
                    }
                    key = path.substring(keyStart, cursor);
                }
                NBTTagCompound keyFilter = null;
                if (cursor < path.length() && path.charAt(cursor) == '{') {
                    ParsedFilter parsed = parseFilter(path, cursor);
                    keyFilter = parsed.filter;
                    cursor = parsed.end;
                }
                tokens.add(Token.key(key, keyFilter));
            }
            first = false;
            if (cursor < path.length()) {
                if (path.charAt(cursor) == '[') {
                    continue;
                }
                if (path.charAt(cursor) != '.' || ++cursor == path.length()) {
                    throw new IllegalArgumentException(path);
                }
            }
        }
        return new FFDNbtPath(tokens);
    }

    private static ParsedString parseQuoted(String path, int start) {
        char quote = path.charAt(start);
        StringBuilder value = new StringBuilder();
        int cursor = start + 1;
        while (cursor < path.length()) {
            char character = path.charAt(cursor++);
            if (character == '\\') {
                if (cursor == path.length()) {
                    throw new IllegalArgumentException(path);
                }
                value.append(path.charAt(cursor++));
            } else if (character == quote) {
                return new ParsedString(value.toString(), cursor);
            } else {
                value.append(character);
            }
        }
        throw new IllegalArgumentException(path);
    }

    private static ParsedToken parseBracket(String path, int start) {
        int depth = 0;
        char quote = 0;
        boolean escaped = false;
        for (int cursor = start + 1; cursor < path.length(); cursor++) {
            char character = path.charAt(cursor);
            if (quote != 0) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == quote) {
                    quote = 0;
                }
                continue;
            }
            if (character == '"' || character == '\'') {
                quote = character;
            } else if (character == '{') {
                depth++;
            } else if (character == '}') {
                depth--;
                if (depth < 0) {
                    throw new IllegalArgumentException(path);
                }
            } else if (character == ']' && depth == 0) {
                String index = path.substring(start + 1, cursor);
                if (index.isEmpty()) {
                    return new ParsedToken(Token.wildcard(), cursor + 1);
                }
                if (index.charAt(0) == '{' && index.charAt(index.length() - 1) == '}') {
                    return new ParsedToken(Token.filter(parseCompound(index)), cursor + 1);
                }
                try {
                    return new ParsedToken(Token.index(Integer.parseInt(index)), cursor + 1);
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException(path);
                }
            }
        }
        throw new IllegalArgumentException(path);
    }

    private static boolean isAllowedInUnquotedName(char character) {
        return character != ' ' && character != '"' && character != '\''
                && character != '[' && character != ']' && character != '.'
                && character != '{' && character != '}';
    }

    List<NBTBase> get(NBTTagCompound root) {
        List<NBTBase> current = Collections.singletonList(root);
        for (Token token : tokens) {
            current = select(current, token);
            if (current.isEmpty()) {
                break;
            }
        }
        return current;
    }

    int set(NBTTagCompound root, NBTBase value) {
        List<NBTBase> parents = getOrCreateParents(root);
        Token last = tokens.get(tokens.size() - 1);
        int changed = 0;
        for (NBTBase parent : parents) {
            if (last.kind == Token.KEY && parent instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) parent;
                NBTBase existing = compound.getTag(last.key);
                if (last.filter != null && (existing == null || !matchesCompound(last.filter, existing))) {
                    continue;
                }
                if (!value.equals(existing)) {
                    compound.setTag(last.key, value.copy());
                    changed++;
                }
            } else if (last.kind == Token.INDEX && parent instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) parent;
                int index = resolveIndex(last.index, list.tagCount());
                if (index >= 0 && index < list.tagCount()
                        && (list.getTagType() == 0 || list.getTagType() == value.getId())
                        && !value.equals(list.get(index))) {
                    list.set(index, value.copy());
                    changed++;
                }
            } else if (last.kind == Token.WILDCARD && parent instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) parent;
                if (list.getTagType() != 0 && list.getTagType() != value.getId()) {
                    continue;
                }
                if (list.tagCount() == 0) {
                    list.appendTag(value.copy());
                    changed++;
                } else {
                    for (int index = 0; index < list.tagCount(); index++) {
                        if (!value.equals(list.get(index))) {
                            list.set(index, value.copy());
                            changed++;
                        }
                    }
                }
            } else if (last.kind == Token.FILTER && parent instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) parent;
                if (list.tagCount() == 0) {
                    list.appendTag(value.copy());
                    changed++;
                } else {
                    for (int index = 0; index < list.tagCount(); index++) {
                        NBTBase existing = list.get(index);
                        if (matchesCompound(last.filter, existing) && !value.equals(existing)
                                && list.getTagType() == value.getId()) {
                            list.set(index, value.copy());
                            changed++;
                        }
                    }
                }
            }
        }
        return changed;
    }

    int merge(NBTTagCompound root, NBTTagCompound value) {
        int changed = 0;
        for (NBTBase target : getOrCreate(root, new NBTTagCompound())) {
            if (target instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) target;
                NBTTagCompound before = compound.copy();
                compound.merge(value);
                if (!compound.equals(before)) {
                    changed++;
                }
            }
        }
        return changed;
    }

    int insert(NBTTagCompound root, int requestedIndex, List<NBTBase> values)
            throws CommandException {
        int changed = 0;
        for (NBTBase target : getOrCreate(root, new NBTTagList())) {
            if (!(target instanceof NBTTagList)) {
                continue;
            }
            if (values.isEmpty()) {
                continue;
            }
            NBTTagList list = (NBTTagList) target;
            int size = list.tagCount();
            long resolved = requestedIndex < 0 ? (long) size + requestedIndex + 1L : requestedIndex;
            if (resolved < 0L || resolved > size) {
                throw new CommandException("commands.farmers_future_delight.data.invalid_index",
                        requestedIndex);
            }
            List<NBTBase> updated = new ArrayList<>();
            for (int index = 0; index < size; index++) {
                updated.add(list.get(index).copy());
            }
            int insertion = (int) resolved;
            int listType = list.getTagType();
            boolean inserted = false;
            for (NBTBase value : values) {
                if (listType != 0 && listType != value.getId()) {
                    continue;
                }
                if (insertion > updated.size()) {
                    throw new CommandException("commands.farmers_future_delight.data.invalid_index",
                            insertion);
                }
                updated.add(insertion++, value.copy());
                listType = value.getId();
                inserted = true;
            }
            if (!inserted) {
                continue;
            }
            while (list.tagCount() > 0) {
                list.removeTag(0);
            }
            for (NBTBase value : updated) {
                list.appendTag(value);
            }
            changed++;
        }
        return changed;
    }

    int remove(NBTTagCompound root) {
        List<NBTBase> parents = parents(root);
        Token last = tokens.get(tokens.size() - 1);
        int removed = 0;
        for (NBTBase parent : parents) {
            if (last.kind == Token.KEY && parent instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) parent;
                NBTBase value = compound.getTag(last.key);
                if (value != null && (last.filter == null || matchesCompound(last.filter, value))) {
                    compound.removeTag(last.key);
                    removed++;
                }
            } else if (last.kind == Token.INDEX && parent instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) parent;
                int index = resolveIndex(last.index, list.tagCount());
                if (index >= 0 && index < list.tagCount()) {
                    list.removeTag(index);
                    removed++;
                }
            } else if (last.kind == Token.WILDCARD && parent instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) parent;
                removed += list.tagCount();
                while (list.tagCount() > 0) {
                    list.removeTag(0);
                }
            } else if (last.kind == Token.FILTER && parent instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) parent;
                for (int index = list.tagCount() - 1; index >= 0; index--) {
                    if (matchesCompound(last.filter, list.get(index))) {
                        list.removeTag(index);
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    private List<NBTBase> parents(NBTTagCompound root) {
        if (tokens.size() == 1) {
            return Collections.singletonList(root);
        }
        List<NBTBase> current = Collections.singletonList(root);
        for (int index = 0; index < tokens.size() - 1; index++) {
            current = select(current, tokens.get(index));
        }
        return current;
    }

    private List<NBTBase> getOrCreateParents(NBTTagCompound root) {
        List<NBTBase> current = Collections.<NBTBase>singletonList(root);
        for (int index = 0; index < tokens.size() - 1; index++) {
            current = getOrCreate(current, tokens.get(index), preferredParent(tokens.get(index + 1)));
            if (current.isEmpty()) {
                break;
            }
        }
        return current;
    }

    private List<NBTBase> getOrCreate(NBTTagCompound root, NBTBase value) {
        List<NBTBase> parents = getOrCreateParents(root);
        return getOrCreate(parents, tokens.get(tokens.size() - 1), value);
    }

    private static NBTBase preferredParent(Token token) {
        return token.kind == Token.KEY ? new NBTTagCompound() : new NBTTagList();
    }

    private static List<NBTBase> getOrCreate(List<NBTBase> sources, Token token,
                                              NBTBase child) {
        List<NBTBase> selected = new ArrayList<>();
        for (NBTBase source : sources) {
            if (token.kind == Token.ROOT_FILTER && matchesCompound(token.filter, source)) {
                selected.add(source);
            } else if (token.kind == Token.KEY && source instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) source;
                NBTBase value = compound.getTag(token.key);
                if (value == null) {
                    value = token.filter == null ? child.copy() : token.filter.copy();
                    compound.setTag(token.key, value);
                    selected.add(value);
                } else if (token.filter == null || matchesCompound(token.filter, value)) {
                    selected.add(value);
                }
            } else if (token.kind == Token.INDEX && source instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) source;
                int index = resolveIndex(token.index, list.tagCount());
                if (index >= 0 && index < list.tagCount()) {
                    selected.add(list.get(index));
                }
            } else if (token.kind == Token.WILDCARD && source instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) source;
                if (list.tagCount() == 0) {
                    list.appendTag(child.copy());
                    selected.add(list.get(0));
                } else {
                    for (int index = 0; index < list.tagCount(); index++) {
                        selected.add(list.get(index));
                    }
                }
            } else if (token.kind == Token.FILTER && source instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) source;
                boolean found = false;
                for (int index = 0; index < list.tagCount(); index++) {
                    NBTBase value = list.get(index);
                    if (matchesCompound(token.filter, value)) {
                        selected.add(value);
                        found = true;
                    }
                }
                if (!found) {
                    NBTBase value = token.filter.copy();
                    list.appendTag(value);
                    selected.add(value);
                }
            }
        }
        return selected;
    }

    private static List<NBTBase> select(List<NBTBase> sources, Token token) {
        List<NBTBase> selected = new ArrayList<>();
        for (NBTBase source : sources) {
            if (token.kind == Token.ROOT_FILTER && matchesCompound(token.filter, source)) {
                selected.add(source);
            } else if (token.kind == Token.KEY && source instanceof NBTTagCompound) {
                NBTBase value = ((NBTTagCompound) source).getTag(token.key);
                if (value != null && (token.filter == null || matchesCompound(token.filter, value))) {
                    selected.add(value);
                }
            } else if (token.kind == Token.INDEX && source instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) source;
                int index = resolveIndex(token.index, list.tagCount());
                if (index >= 0 && index < list.tagCount()) {
                    selected.add(list.get(index));
                }
            } else if (token.kind == Token.WILDCARD && source instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) source;
                for (int index = 0; index < list.tagCount(); index++) {
                    selected.add(list.get(index));
                }
            } else if (token.kind == Token.FILTER && source instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) source;
                for (int index = 0; index < list.tagCount(); index++) {
                    NBTBase value = list.get(index);
                    if (matchesCompound(token.filter, value)) {
                        selected.add(value);
                    }
                }
            }
        }
        return selected;
    }

    private static int resolveIndex(int requestedIndex, int size) {
        return requestedIndex < 0 ? size + requestedIndex : requestedIndex;
    }

    private static ParsedFilter parseFilter(String path, int start) {
        int depth = 0;
        char quote = 0;
        boolean escaped = false;
        for (int cursor = start; cursor < path.length(); cursor++) {
            char character = path.charAt(cursor);
            if (quote != 0) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == quote) {
                    quote = 0;
                }
            } else if (character == '"' || character == '\'') {
                quote = character;
            } else if (character == '{') {
                depth++;
            } else if (character == '}' && --depth == 0) {
                String text = path.substring(start, cursor + 1);
                return new ParsedFilter(parseCompound(text), cursor + 1);
            }
        }
        throw new IllegalArgumentException(path);
    }

    private static final class ParsedString {
        private final String value;
        private final int end;

        private ParsedString(String value, int end) {
            this.value = value;
            this.end = end;
        }
    }

    private static final class ParsedToken {
        private final Token token;
        private final int end;

        private ParsedToken(Token token, int end) {
            this.token = token;
            this.end = end;
        }
    }

    private static NBTTagCompound parseCompound(String text) {
        try {
            NBTBase value = JsonToNBT.getTagFromJson(text);
            if (value instanceof NBTTagCompound) {
                return (NBTTagCompound) value;
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException(text);
        }
        throw new IllegalArgumentException(text);
    }

    static boolean matchesCompound(NBTTagCompound expected, NBTBase actual) {
        if (!(actual instanceof NBTTagCompound)) {
            return false;
        }
        NBTTagCompound compound = (NBTTagCompound) actual;
        for (String key : expected.getKeySet()) {
            NBTBase expectedValue = expected.getTag(key);
            NBTBase actualValue = compound.getTag(key);
            if (actualValue == null || !matchesTag(expectedValue, actualValue)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesTag(NBTBase expected, NBTBase actual) {
        if (expected instanceof NBTTagCompound) {
            return matchesCompound((NBTTagCompound) expected, actual);
        }
        if (expected instanceof NBTTagList && actual instanceof NBTTagList) {
            NBTTagList expectedList = (NBTTagList) expected;
            NBTTagList actualList = (NBTTagList) actual;
            if (expectedList.tagCount() == 0) {
                return actualList.tagCount() == 0;
            }
            if (expectedList.tagCount() > actualList.tagCount()) {
                return false;
            }
            for (int expectedIndex = 0; expectedIndex < expectedList.tagCount(); expectedIndex++) {
                boolean found = false;
                for (int actualIndex = 0; actualIndex < actualList.tagCount(); actualIndex++) {
                    if (matchesTag(expectedList.get(expectedIndex), actualList.get(actualIndex))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        return expected.equals(actual);
    }

    private static final class Token {
        private static final int KEY = 0;
        private static final int INDEX = 1;
        private static final int WILDCARD = 2;
        private static final int FILTER = 3;
        private static final int ROOT_FILTER = 4;
        private final int kind;
        private final String key;
        private final int index;
        private final NBTTagCompound filter;

        private Token(int kind, String key, int index, NBTTagCompound filter) {
            this.kind = kind;
            this.key = key;
            this.index = index;
            this.filter = filter;
        }

        private static Token key(String key, NBTTagCompound filter) {
            return new Token(KEY, key, -1, filter);
        }

        private static Token index(int index) {
            return new Token(INDEX, null, index, null);
        }

        private static Token wildcard() {
            return new Token(WILDCARD, null, -1, null);
        }

        private static Token filter(NBTTagCompound filter) {
            return new Token(FILTER, null, -1, filter);
        }

        private static Token rootFilter(NBTTagCompound filter) {
            return new Token(ROOT_FILTER, null, -1, filter);
        }
    }

    private static final class ParsedFilter {
        private final NBTTagCompound filter;
        private final int end;

        private ParsedFilter(NBTTagCompound filter, int end) {
            this.filter = filter;
            this.end = end;
        }
    }
}
