package com.example.map;

import com.example.map.GuidingServiceListener;

interface GuidingServiceApi {

	void addListener(GuidingServiceListener listener);

	void removeListener(GuidingServiceListener listener);
}