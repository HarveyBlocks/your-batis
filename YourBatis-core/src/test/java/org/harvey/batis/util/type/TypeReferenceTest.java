package org.harvey.batis.util.type;

import junit.framework.TestCase;

public class TypeReferenceTest extends TestCase {
    static class StringTypeReference extends TypeReference<String> {
    }

    static class StringTypeReference1 extends StringTypeReference {
    }

    static class StringTypeReference2 extends StringTypeReference1 {
    }

    static class StringTypeReference3 extends StringTypeReference2 {
    }

    static class StringTypeReference4 extends StringTypeReference3 {
    }

    static class StringTypeReference5 extends StringTypeReference4 {
    }


    public void testReference() {
        StringTypeReference5 reference = new StringTypeReference5();
        System.out.println("reference = " + reference);
    }
}