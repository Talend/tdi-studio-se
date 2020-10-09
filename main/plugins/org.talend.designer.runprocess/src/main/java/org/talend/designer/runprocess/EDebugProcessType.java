//============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2019 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
//============================================================================
package org.talend.designer.runprocess;

public enum EDebugProcessType {

   DI("DI"), //$NON-NLS-1$
   CAMEL("CAMEL"); //$NON-NLS-1$

    private String debugType;

    private EDebugProcessType(String debugType) {

    }

    public String getDebugType() {
        return debugType;
    }
    
    public static EDebugProcessType findDebugType(String typeName) {
        for(EDebugProcessType e: values()){
            if(e.debugType.equals(typeName)) {
                return e;
            }
        }
        return DI;
    }
}
