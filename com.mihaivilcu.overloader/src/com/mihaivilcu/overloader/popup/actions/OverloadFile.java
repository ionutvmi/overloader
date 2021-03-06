package com.mihaivilcu.overloader.popup.actions;

import java.util.*;


import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.*;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class OverloadFile implements IObjectActionDelegate {

	private Shell shell;
	private ITreeSelection currentSelection;

	/**
	 * Constructor for Action1.
	 */
	public OverloadFile() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new WorkbenchLabelProvider());

		dialog.setElements(getProjects());

		dialog.setHelpAvailable(false);
		dialog.setMessage(Messages.OverloadFile_selectbox_message);
		dialog.setTitle(Messages.OverloadFile_select_destinaion);
		// user pressed cancel
		if (dialog.open() != Window.OK) {
			return;
		}

		IProject project = (IProject) dialog.getResult()[0];
		this.overloadToProject(project);
	}

	/**
	 * Loops over the selected files and performs the overload.
	 * 
	 * @param project
	 */
	protected void overloadToProject(IProject project) {
		// get all the selected files
		Iterator<IFile> files = currentSelection.iterator();

		while (files.hasNext()) {
			IFile elem = files.next();
			IPath path = project.getFullPath().append(elem.getProjectRelativePath());

			try {
				this.createFolders(project, elem.getParent().getProjectRelativePath());
				elem.copy(path, false, null);
				MessageDialog.openInformation(shell, Messages.OverloadFile_success_title, Messages.OverloadFile_success_message);
				this.openFile(path);

			} catch (CoreException e) {
				MessageDialog.openError(shell, Messages.OverloadFile_error_title, e.getMessage());
				return;
			}

		}

	}

	/**
	 * Creates the folder structure for the specified path.
	 * 
	 * @param project
	 * @param path
	 * @throws CoreException
	 */
	protected void createFolders(IProject project, IPath path) throws CoreException {
		for (int i = path.segmentCount() - 1; i >= 0; i--) {
			IFolder folder = project.getFolder(path.removeLastSegments(i));
			if (!folder.exists()) {
				folder.create(true, true, null);
			}
		}
	}

	/**
	 * Opens the specified path in the editor
	 * 
	 * @param path
	 */
	public void openFile(IPath path) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

		if (!newFile.exists()) {
			return;
		}

		if (window != null) {
			IWorkbenchPage page = window.getActivePage();

			try {
				IDE.openEditor(page, newFile);
			} catch (PartInitException e) {
				// noop
			}
		}
	}

	/**
	 * Returns a list of all the available projects.
	 * 
	 * @return
	 */
	public IProject[] getProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		// active project
		IProject activeProject = ((IFile)currentSelection.getFirstElement()).getProject();
		
		List<IProject> results = new ArrayList<IProject>();

		for ( IProject p : projects ) {
			if (p == activeProject || p.getName().equals("RemoteSystemsTempFiles")) {
				continue;
			}

			results.add(p);
		}
		
		return results.toArray(new IProject[results.size()]);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		currentSelection = (ITreeSelection)selection;
	}
	


}
