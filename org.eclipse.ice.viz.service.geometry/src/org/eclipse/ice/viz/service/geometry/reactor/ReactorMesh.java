/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.geometry.reactor;

import org.eclipse.ice.viz.service.modeling.AbstractController;
import org.eclipse.ice.viz.service.modeling.ShapeController;
import org.eclipse.ice.viz.service.modeling.ShapeMesh;

/**
 * The internal representation of a plant component
 * 
 * @author Robert Smith
 *
 */
public class ReactorMesh extends ShapeMesh {

	/**
	 * The default constructor
	 */
	public ReactorMesh() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ice.viz.service.modeling.ShapeComponent#addEntityByCategory(
	 * org.eclipse.ice.viz.service.modeling.AbstractController,
	 * java.lang.String)
	 */
	@Override
	public void addEntityByCategory(AbstractController entity,
			String category) {

		// If the entity is a pipe, set this shape as its parent, so
		// that when the reactor is transformed all core channels will also be
		// transformed
		if (entity instanceof PipeController) {
			((PipeController) entity).setParent((ShapeController) controller);
		}

		super.addEntityByCategory(entity, category);

	}
}