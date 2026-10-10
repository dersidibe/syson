/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.syson.sysml.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.MembershipExpose;
import org.eclipse.syson.sysml.OwningMembership;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.ViewUsage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ViewUsageImpl}.
 *
 * @author sdibe
 */
public class ViewUsageImplTest {

    @Test
    @DisplayName("GIVEN a recursive expose of a nested package, WHEN getExposedElement is called, THEN the exposed element and all its contents are returned")
    public void getExposedElementRecursive() {
        Package root = this.createPackage("root");
        Package child = this.createPackage("child");
        OwningMembership childMembership = this.addOwned(root, child);
        Package grandChild = this.createPackage("grandChild");
        this.addOwned(child, grandChild);

        ViewUsage view = this.createRecursiveExposeView(childMembership);

        assertThat(view.getExposedElement()).containsExactly(child, grandChild);
    }

    @Test
    @DisplayName("GIVEN a containment cycle under a recursively exposed element, WHEN getExposedElement is called, THEN it terminates")
    public void getExposedElementWithContainmentCycle() {
        Package root = this.createPackage("root");
        Package child = this.createPackage("child");
        OwningMembership childMembership = this.addOwned(root, child);
        // Make root contained by its own child: root -> child -> root.
        this.addOwned(child, root);

        ViewUsage view = this.createRecursiveExposeView(childMembership);

        var exposed = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> view.getExposedElement());
        assertThat(exposed).contains(child, root);
    }

    private ViewUsage createRecursiveExposeView(OwningMembership exposedMembership) {
        ViewUsage view = SysmlFactory.eINSTANCE.createViewUsage();
        MembershipExpose expose = SysmlFactory.eINSTANCE.createMembershipExpose();
        expose.setImportedMembership(exposedMembership);
        expose.setIsRecursive(true);
        view.getOwnedRelationship().add(expose);
        return view;
    }

    private Package createPackage(String name) {
        Package pack = SysmlFactory.eINSTANCE.createPackage();
        pack.setDeclaredName(name);
        return pack;
    }

    private OwningMembership addOwned(Element owner, Element owned) {
        OwningMembership membership = SysmlFactory.eINSTANCE.createOwningMembership();
        owner.getOwnedRelationship().add(membership);
        membership.getOwnedRelatedElement().add(owned);
        return membership;
    }
}
