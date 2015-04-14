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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

public class GeobufDirectoryDataStoreTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void dataStore() throws Exception {

        // Create a directory
        File directory = temporaryFolder.newFolder();

        // Copy over some PBF files
        String[] pbfNames = {
                "lines", "points", "polygons"
        };
        for(String name : pbfNames) {
            File file = DataUtilities.urlToFile(getClass().getClassLoader().getResource("org/geotools/data/geobuf/" + name + ".pbf"));
            Files.copy(file.toPath(), new File(directory, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // Get a DataStore
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", directory);
        DataStore store = DataStoreFinder.getDataStore(params);

        // Get layers
        List<String> names = Arrays.asList(store.getTypeNames());
        for(String name : pbfNames) {
            assertTrue(names.contains(name));
        }

        // Make sure we can get layers
        for(String name : names) {
            SimpleFeatureSource fs = store.getFeatureSource(name);
            assertNotNull(fs.getBounds());
            assertNotNull(fs.getSchema());
            assertTrue(fs.getCount(Query.ALL) > 0);
        }

        // Write a new Layer
        SimpleFeatureType featureType = DataUtilities.createType("locations", "geom:Point,name:String,id:int");
        store.createSchema(featureType);
        SimpleFeatureStore featureStore = (SimpleFeatureStore) store.getFeatureSource("locations");
        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
        SimpleFeature feature1 = SimpleFeatureBuilder.build(
                featureType, new Object[]{gf.createPoint(new Coordinate(-8.349609375, 14.349547837185362)), "ABC", 1},
                "location.1"
        );
        SimpleFeature feature2 = SimpleFeatureBuilder.build(
                featureType, new Object[]{gf.createPoint(new Coordinate(-18.349609375, 24.349547837185362)), "DEF", 2},
                "location.2"
        );
        SimpleFeatureCollection collection = DataUtilities.collection(new SimpleFeature[]{feature1,feature2});
        featureStore.addFeatures(collection);
        assertEquals(2, featureStore.getCount(Query.ALL));
    }

}
