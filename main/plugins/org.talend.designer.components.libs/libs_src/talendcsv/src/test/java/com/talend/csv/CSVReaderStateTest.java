package com.talend.csv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class CSVReaderStateTest {

    private static final CSVConfig config = new CSVConfig();

    @BeforeAll
    public static void init() {
        config.setSeparator(',');
        config.setEscapechar('\\');
        config.setQuotechar('"');
    }


    @Test
    public void quotedField() {
        checkField(new CSVReader.QuotedFieldState(null), "Hello\",", "Hello");
        checkField(new CSVReader.QuotedFieldState(null), "He\\nllo\",", "He\nllo");
        checkField(new CSVReader.QuotedFieldState(null), "Hello\"toto\",", "Hello\"toto");
        checkField(new CSVReader.QuotedFieldState(null), "Hello\" toto\"  ,", "Hello\" toto");
    }

    @Test
    public void outsideField() {
        CSVReader.State state = new CSVReader.StartState(null);
        String source = "Hello,\"World\" , next  \n Nex,\"World \t\" , ne\txt  \n";

        final List<List<String>> records = new ArrayList<>();
        CSVReader.ResultAction action = new CSVReader.ResultAction((List<String> f) -> {
            records.add(new ArrayList<>(f));
        });
        state = this.accept(state, source, action);

        Assertions.assertEquals(2, records.size());

        List<String> rec1 = records.get(0);
        Assertions.assertEquals(3, rec1.size());
        Assertions.assertEquals("Hello", rec1.get(0));
        Assertions.assertEquals("World", rec1.get(1));
        Assertions.assertEquals(" next", rec1.get(2));

        List<String> rec2 = records.get(1);
        Assertions.assertEquals(3, rec2.size());
        Assertions.assertEquals(" Nex", rec2.get(0));
        Assertions.assertEquals("World \t", rec2.get(1));
        Assertions.assertEquals(" ne\txt", rec2.get(2));

    }

    private void checkField(CSVReader.State state, String from, String to) {
        CSVReader.ResultAction action = new CSVReader.ResultAction(null);
        state = this.accept(state, from, action);

        List<String> fields = action.getFields();
        Assertions.assertEquals(1, fields.size());
        Assertions.assertEquals(to, fields.get(0));
        Assertions.assertNull(state);
    }

    private CSVReader.State accept(CSVReader.State state, String value, CSVReader.ResultAction action)  {
        for (int i = 0; i < value.length(); i++) {
            CSVReader.State state2 = state.accept(value.charAt(i), config, action);
            if (state2 == null) {
                System.out.println("Err " + state.getClass().getName());
            }
            state = state2;
        }
        return state;
    }



}