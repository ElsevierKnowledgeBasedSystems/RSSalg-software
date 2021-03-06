/** 	
 * Name: RandomSplit.java
 * 
 * Purpose:  Creates a balanced random split of features in two views. Balanced split - views have the same number of features.
 * 
 * Author: Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
 * 
 * Copyright: (c) 2016 Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
 * 
 * This file is a part of RSSalg software, a flexible, highly configurable tool for experimenting 
 * with co-training based techniques. RSSalg Software encompasses the implementation of 
 * co-training and RSSalg, a co-training based technique that can be applied to single-view 
 * datasets published in the paper: 
 * 
 * Slivka, J., Kovacevic, A. and Konjovic, Z., 2013. 
 * Combining Co-Training with Ensemble Learning for Application on Single-View Natural 
 * Language Datasets. Acta Polytechnica Hungarica, 10(2).
 *   
 * RSSalg software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RSSalg software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package featureSplit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import algorithms.co_training.CoTrainingData;
import experimentSetting.DatasetSettings;
import featureSplit.utils.Feature;
import featureSplit.utils.FeatureGraph;
import featureSplit.utils.FeatureSplit;

/**
 * Creates a balanced random split of features in two views. Balanced split - views have the same number of features.
 */
public class RandomSplit implements SplitterIF {
	/**
	 * The generated feature split
	 */
	FeatureSplit featureSplit = null;

	/**
	 * Generates a new random split of features in the two balanced views
	 * @param data data to split into views
	 * @param rand instance of random number generator to use
	 * @return the new random feature split
	 */
	public FeatureSplit generateNewSplit(CoTrainingData data, Random rand){
		
		int idAttIndex = data.getLabeledData()[0].attribute(DatasetSettings.getInstance().getIdAttributeName()).index(); 
		int classAttIndex = data.getLabeledData()[0].classIndex();		
		
		List<Feature> features = new ArrayList<Feature>();
		for(int attInd=0; attInd<data.getLabeledData()[0].numAttributes(); attInd++){
			if(attInd == idAttIndex || attInd == classAttIndex)
				continue; // skip id and class
			String featureName = data.getLabeledData()[0].attribute(attInd).name();
			int featureInd = attInd+1; // +1 because attribute filters count from 1
			features.add(new Feature(featureName, featureInd));
		}
	
		int noAttsInView1 = features.size()/2; 
		int noAttsInView2 =  features.size() - noAttsInView1;
		
		int featuresInView1 = 0;
		int featuresInView2 = 0;
		FeatureSplit split = new FeatureSplit();
		
//		String res = "";
		for(int i=0; i<features.size(); i++){
			int view = rand.nextInt(2);
			if(view==0){
				if(featuresInView1<noAttsInView1){
					featuresInView1++; // feature i stays in first view		
					split.addFeatureToView(features.get(i), 0);
//					res += 0;
				}else{ // move feature i to second view
					featuresInView2++;
					split.addFeatureToView(features.get(i), 1);		
//					res += 1;
				}
			}else{
				if(featuresInView2<noAttsInView2){
					featuresInView2++; // move feature i to second view
					split.addFeatureToView(features.get(i), 1);
//					res += 1;
				}else{ // feature i stays in first view
					featuresInView1++;
					split.addFeatureToView(features.get(i), 0);
//					res += 0;
				}
			}			
		}
		
//		System.out.println("Created " + res);

		return split;
	}

	
	/**
	 * Creates a balanced random split of features in two views. Balanced split - views have the same number of features.  
	 * @param graph this parameter is ignored - random feature split does not rely on a certain measure so it does not need a graph
	 * @param data data to split in the views
	 * @param rand an instance of random number generator to use
	 * @param splitNo the number of split
	 * @return the feature split
	 * @throws Exception if there was an error creating the split
	 */
	public FeatureSplit splitDatasets(FeatureGraph graph, CoTrainingData data, Random rand, int splitNo)
			throws Exception {
		data.mergeViews();
		if(featureSplit == null) // to ensure that all folds use the same split only one split is created
			featureSplit = generateNewSplit(data, rand);
		Set<Integer> indicesOfFeaturesToMove = featureSplit.getFeatureIndicesForView(1);
		data.moveAttributes(0, 1, indicesOfFeaturesToMove);
		return featureSplit;
	}


	public String getName() {
		return "Random";
	}

}
