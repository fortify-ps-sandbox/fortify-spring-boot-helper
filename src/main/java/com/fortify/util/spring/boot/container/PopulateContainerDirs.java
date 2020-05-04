/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.util.spring.boot.container;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class PopulateContainerDirs {
	public static final void populateContainerDirs() throws IOException {
		if ( "true".equals(System.getProperty("populateContainerDirs")) ) {
			Path sourcePath = Paths.get(System.getProperty("populateContainerDirs.sourceDir", "/default"));
			
			if ( Files.exists(sourcePath) ) {
				Files.list(sourcePath).forEach(PopulateContainerDirs::populate);
			}
		}
	}
	
	private static final void populate(Path sourcePath) {
		Path targetPath = Paths.get(System.getProperty("populateContainerDirs.targetDir", "/")).resolve(sourcePath.getFileName());
		try {
			if (isTargetNotPresentOrEmpty(targetPath)) {
				copy(sourcePath, targetPath);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error copying files", e);
		}
	}

	private static boolean isTargetNotPresentOrEmpty(Path targetPath) throws IOException {
		return !Files.exists(targetPath) || 
				(Files.isDirectory(targetPath) && !Files.list(targetPath).anyMatch(PopulateContainerDirs::anyButEmpty));
	}
	
	private static final boolean anyButEmpty(Path p) {
		return !".empty".equals(p.getFileName().toString());		
	}
	
	private static final void copy(Path sourcePath, Path targetPath) throws IOException {
		System.out.println(String.format("Copying %s to %s", sourcePath.toString(), targetPath.toString()));
		if ( Files.isDirectory(sourcePath) ) {
			copyFolder(sourcePath, targetPath);
		} else {
			Files.copy(sourcePath, targetPath);
		}
	}

	private static final void copyFolder(Path sourcePath, Path targetPath, CopyOption... options) throws IOException {
        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
