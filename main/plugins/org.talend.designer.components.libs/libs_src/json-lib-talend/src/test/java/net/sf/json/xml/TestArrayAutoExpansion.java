/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.json.xml;

import net.sf.json.JSON;
import org.custommonkey.xmlunit.XMLTestCase;


/**
 * @author Tobias Sodergren <tobias.sodergren@infomaker.se>
 */
public class TestArrayAutoExpansion extends XMLTestCase {

   private static final String FIXTURE = "<Document DOMVersion=\"8.0\" Self=\"d\">" +
         "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 5.3-c011 66.145661, 2012/02/06-14:56:27\">" +
         "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
         "<rdf:Description rdf:about=\"\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
         "<dc:format>application/x-indesign</dc:format>" +
         "</rdf:Description>" +
         "<rdf:Description rdf:about=\"\" xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\">" +
         "<xmp:CreatorTool>Adobe InDesign CS6 (Macintosh)</xmp:CreatorTool>" +
         "</rdf:Description>" +
         "<rdf:Description rdf:about=\"\" xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\">" +
         "<xmpMM:InstanceID>xmp.iid:D093CC710A2068118083AFA2F0AAE3ED</xmpMM:InstanceID>" +
         "</rdf:Description>" +
         "</rdf:RDF>" +
         "</x:xmpmeta>" +
         "</Document>";

   private static final String FIXTURE_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
         "<TextFrame Self=\"u11f6\" ParentStory=\"u11e4\" PreviousTextFrame=\"n\" NextTextFrame=\"n\" ContentType=\"TextType\" NewspilotId=\"0 0\" ImtoolsWritebackType=\"0\" ImtoolsMacroData=\"\" ImtoolsArticleParts=\"103416\" ImtoolsFramedata=\"{21889731;93073;89;1;1;12127681;0;0;0;1;1;Artikel;;103416}\" ImtoolsExportedSelection=\"0\" ImtoolsLayoutId=\"21889731\" ImtoolsArticleId=\"93073\" ImtoolsArticleTypeId=\"89\" ImtoolsArticlePartName=\"Artikel\" ImtoolsArticlePartNumber=\"1\" ImtoolsPartOrder=\"1\" ImtoolsOption=\"\" ImtoolsIsUnresolved=\"true\" ImtoolsContentId=\"0\" ImtoolsContentType=\"0\" ImtoolsTemplateInfoData=\"60 89\" ImtoolsTemplateName=\"Normal\" ImtoolsParagraphData=\"{headline}\" ParentInterfaceChangeCount=\"138508 2034277516 138558 1409276294\" TargetInterfaceChangeCount=\"\" LastUpdatedInterfaceChangeCount=\"\" OverriddenPageItemProps=\"\" HorizontalLayoutConstraints=\"FlexibleDimension FixedDimension FlexibleDimension\" VerticalLayoutConstraints=\"FlexibleDimension FixedDimension FlexibleDimension\" FillColor=\"Color/Paper\" GradientFillStart=\"-140.31496000000004 20\" GradientFillLength=\"280.62992\" GradientFillAngle=\"0\" GradientStrokeStart=\"-140.31496000000004 20\" GradientStrokeLength=\"280.62992\" GradientStrokeAngle=\"0\" ItemLayer=\"u126\" Locked=\"false\" LocalDisplaySetting=\"Default\" GradientFillHiliteLength=\"0\" GradientFillHiliteAngle=\"0\" GradientStrokeHiliteLength=\"0\" GradientStrokeHiliteAngle=\"0\" AppliedObjectStyle=\"ObjectStyle/$ID/[Normal Text Frame]\" Visible=\"true\" Name=\"$ID/\" ItemTransform=\"1 0 0 1 -610.8661466929134 -420.2831501574803\">" +
         "<Properties>" +
         "<PathGeometry>" +
         "<GeometryPathType PathOpen=\"false\">" +
         "<PathPointArray>" +
         "<PathPointType Anchor=\"-140.31496000000004 -29.999999448818926\" LeftDirection=\"-140.31496000000004 -29.999999448818926\" RightDirection=\"-140.31496000000004 -29.999999448818926\"/>" +
         "<PathPointType Anchor=\"-140.31496000000004 20\" LeftDirection=\"-140.31496000000004 20\" RightDirection=\"-140.31496000000004 20\"/>" +
         "<PathPointType Anchor=\"140.31495999999999 20\" LeftDirection=\"140.31495999999999 20\" RightDirection=\"140.31495999999999 20\"/>" +
         "<PathPointType Anchor=\"140.31495999999999 -29.999999448818926\" LeftDirection=\"140.31495999999999 -29.999999448818926\" RightDirection=\"140.31495999999999 -29.999999448818926\"/>" +
         "</PathPointArray>" +
         "</GeometryPathType>" +
         "</PathGeometry>" +
         "</Properties>" +
         "</TextFrame>";

   private static final String FIXTURE_3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
         "<Properties>" +
         "<PathGeometry  class=\"object\">" +
         "<GeometryPathType PathOpen=\"false\" class=\"object\" >" +
         "<PathPointArray class=\"array\">" +
         "<PathPointType Anchor=\"-140.31496000000004 -29.999999448818926\" LeftDirection=\"-140.31496000000004 -29.999999448818926\" RightDirection=\"-140.31496000000004 -29.999999448818926\" class=\"object\"/>" +
         "<PathPointType Anchor=\"-140.31496000000004 20\" LeftDirection=\"-140.31496000000004 20\" RightDirection=\"-140.31496000000004 20\" class=\"object\"/>" +
         "<PathPointType Anchor=\"140.31495999999999 20\" LeftDirection=\"140.31495999999999 20\" RightDirection=\"140.31495999999999 20\" class=\"object\"/>" +
         "<PathPointType Anchor=\"140.31495999999999 -29.999999448818926\" LeftDirection=\"140.31495999999999 -29.999999448818926\" RightDirection=\"140.31495999999999 -29.999999448818926\" class=\"object\"/>" +
         "</PathPointArray>" +
         "</GeometryPathType>" +
         "</PathGeometry>" +
         "</Properties>";


   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestArrayAutoExpansion.class );
   }

   public TestArrayAutoExpansion( String name ) {
      super( name );
   }

   public void testShouldCreateAutomaticExpansionWhenContainingArrayOfObjects() throws Exception {

      XMLSerializer serializer = new XMLSerializer();
      serializer.setTypeHintsEnabled( false );
      serializer.setPerformAutoExpansion( true );
      serializer.setRootName( "Document" );

      JSON jsonRepresentation = serializer.read( FIXTURE );

      final String writtenBack = serializer.write( jsonRepresentation );

      assertXMLEqual( FIXTURE, writtenBack );
   }

   public void test_keep_array_name() throws Exception {
      XMLSerializer serializer = new XMLSerializer();
      serializer.setTypeHintsEnabled( false );
      serializer.setPerformAutoExpansion( true );
      serializer.setKeepArrayName( true );
      serializer.setEscapeLowerChars( true );
      serializer.setRootName( "TextFrame" );

      JSON jsonRepresentation = serializer.read( FIXTURE_2 );
      final String writtenBack = serializer.write( jsonRepresentation );

      assertXMLEqual( FIXTURE_2, writtenBack );
   }

   public void test_keep_array_name_with_type_hints_should_throw_exception() throws Exception {
      XMLSerializer serializer = new XMLSerializer();
      serializer.setTypeHintsEnabled( true );
      serializer.setKeepArrayName( true );

      JSON jsonRepresentation = serializer.read( FIXTURE_3 );
      try{
         serializer.write( jsonRepresentation );
         fail( "Type hints are not compatible with KeepArrayName" );
      }catch( IllegalStateException e ){
         assertEquals( "Type Hints cannot be used together with 'keepArrayName'", e.getMessage() );
      }
   }
}