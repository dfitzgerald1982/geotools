/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2015, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.geobuf;

import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GeobufDirectoryDataStore extends ContentDataStore {

    private File directory;

    private int precision = 6;

    private int dimension = 2;

    public GeobufDirectoryDataStore(File directory, int precision, int dimension) {
        this.directory = directory;
        this.precision = precision;
        this.dimension = dimension;
    }

    protected File getDirectory() {
        return directory;
    }

    protected GeobufDataStore getDataStore(String name) {
        File file = new File(directory, name + ".pbf");
        return new GeobufDataStore(file, precision, dimension);
    }

    @Override
    protected List<Name> createTypeNames() throws IOException {
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".pbf");
            }
        });
        List<Name> names = new ArrayList<>();
        for (File file : files) {
            String name = file.getName();
            names.add(new NameImpl(name.substring(0, name.lastIndexOf('.'))));
        }
        return names;
    }

    @Override
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        getDataStore(featureType.getTypeName()).createSchema(featureType);
    }

    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry contentEntry) throws IOException {
        return getDataStore(contentEntry.getTypeName()).createFeatureSource(contentEntry);
    }
}
