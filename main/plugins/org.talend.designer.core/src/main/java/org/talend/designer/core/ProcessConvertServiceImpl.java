package org.talend.designer.core;

import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.impl.ProcessItemImpl;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.convert.IProcessConvertService;
import org.talend.designer.core.convert.ProcessConverterType;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.model.RepositoryNode;

public class ProcessConvertServiceImpl implements IProcessConvertService {

    public ProcessConvertServiceImpl() {
    }

    @Override
    public IProcess getProcessFromItem(Item item, boolean loadScreenshots) {
        if ((ProcessItemImpl.class == item.getClass()) || (ProcessItemImpl.class == item.getClass().getSuperclass())) {
            Process process = null;
            process = new Process(item.getProperty());
            process.loadXmlFile(loadScreenshots);
            return process;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.IProcessConvertService#convertToProcess(org.talend.core.model.properties.Item,
     * org.talend.core.model.repository.IRepositoryViewObject)
     */
    @Override
    public Item convertToProcess(Item item, IRepositoryViewObject repViewObject) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.IProcessConvertService#convertFromProcess(org.talend.core.model.properties.Item,
     * org.talend.core.model.repository.IRepositoryViewObject)
     */
    @Override
    public Item convertFromProcess(Item item, IRepositoryViewObject repViewObject) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.IProcessConvertService#isOriginalItemDeleted()
     */
    @Override
    public boolean isOriginalItemDeleted() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.IProcessConvertService#isNewItemCreated()
     */
    @Override
    public boolean isNewItemCreated() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.IProcessConvertService#getConverterType()
     */
    @Override
    public ProcessConverterType getConverterType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Item convertFromProcess(Item item, IRepositoryViewObject repViewObject, RepositoryNode targetNode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Item convertToProcess(Item item, IRepositoryViewObject repViewObject, RepositoryNode targetNode) {
        // TODO Auto-generated method stub
        return null;
    }

}
