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

package net.sf.json.sample;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class MediaListBean {
   private Object media;
   private List media2;

   public Object getMedia() {
      return media;
   }

   public List getMedia2() {
      return media2;
   }

   public void setMedia( Object media ) {
      this.media = media;
   }

   public void setMedia2( List media2 ) {
      this.media2 = media2;
   }

   public String toString() {
      return ToStringBuilder.reflectionToString( this, ToStringStyle.MULTI_LINE_STYLE );
   }
}