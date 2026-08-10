package de.uka.ipd.sdq.pcm.transformations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;

public abstract class AllInstancesTransformer<T> {

    private EObject rootNode;
    private EClassifier eClass;

    public AllInstancesTransformer(EClassifier eClass, EObject rootNode) {
        super();
        this.rootNode = rootNode;
        this.eClass = eClass;
    }

    @SuppressWarnings("unchecked")
    public void transform() throws JobFailedException {
        try {
            for (EObject eObject : allInstances()) {
                transform((T) eObject);
            }
        } catch (Exception ex) {
            throw new JobFailedException("Transformation failed", ex);
        }
    }

    /**
     * Collects the root node and everything in its containment tree that is an instance of the
     * configured classifier.
     */
    private List<EObject> allInstances() {
        List<EObject> result = new ArrayList<>();
        if (eClass.isInstance(rootNode)) {
            result.add(rootNode);
        }
        for (TreeIterator<EObject> contents = rootNode.eAllContents(); contents.hasNext();) {
            EObject candidate = contents.next();
            if (eClass.isInstance(candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }

    protected abstract void transform(T object);
}
