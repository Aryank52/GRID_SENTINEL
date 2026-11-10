// Custom type declarations for maplibre-gl
// Supplements @types/maplibre-gl (which is based on mapboxgl types) with missing exports

import * as mapboxgl from 'mapboxgl';

// Re-export all mapboxgl types
export * from 'mapboxgl';

// Additional types and exports specific to maplibre-gl
declare module 'maplibre-gl' {
    // FilterSpecification is used in map.setFilter()
    export type FilterSpecification = any[];

    // MapGeoJSONFeature is the feature type returned by map.queryRenderedFeatures()
    export type MapGeoJSONFeature = mapboxgl.MapboxGeoJSONFeature;

    // LayerSpecification is the layer object passed to map.addLayer()
    export type LayerSpecification = mapboxgl.AnyLayer;

    // GeoJSONSourceSpecification for GeoJSON source options
    export type GeoJSONSourceSpecification = mapboxgl.GeoJSONSourceRaw;

    // FilterSpecification for map.setFilter()
    export type FilterSpecification = any[];

    // Source types
    export type AnySourceData = mapboxgl.AnySourceData;
    export type AnySourceImpl = mapboxgl.AnySourceImpl;
    export type AnyLayer = mapboxgl.AnyLayer;
    export type AnyLayout = mapboxgl.AnyLayout;
    export type AnyPaint = mapboxgl.AnyPaint;

    // Map options and types
    export interface MapOptions extends mapboxgl.MapOptions {}
    export type Map = mapboxgl.Map;
    export type MapMouseEvent = mapboxgl.MapMouseEvent;
    export type MapTouchEvent = mapboxgl.MapTouchEvent;
    export type MapWheelEvent = mapboxgl.MapWheelEvent;
    export type MapLayerMouseEvent = mapboxgl.MapLayerMouseEvent;
    export type MapLayerTouchEvent = mapboxgl.MapLayerTouchEvent;
    export type MapEventType = mapboxgl.MapEventType;
    export type MapLayerEventType = mapboxgl.MapLayerEventType;

    // GeoJSON types
    export type GeoJSONSourceOptions = mapboxgl.GeoJSONSourceOptions;
    export type GeoJSONSourceRaw = mapboxgl.GeoJSONSourceRaw;
    export type GeoJSONSource = mapboxgl.GeoJSONSource;

    // Layer specifications
    export type LayerSpecification = mapboxgl.AnyLayer;
    export type Layer = mapboxgl.Layer;
    export type BackgroundLayer = mapboxgl.BackgroundLayer;
    export type FillLayer = mapboxgl.FillLayer;
    export type LineLayer = mapboxgl.LineLayer;
    export type SymbolLayer = mapboxgl.SymbolLayer;
    export type CircleLayer = mapboxgl.CircleLayer;
    export type HeatmapLayer = mapboxgl.HeatmapLayer;
    export type FillExtrusionLayer = mapboxgl.FillExtrusionLayer;
    export type RasterLayer = mapboxgl.RasterLayer;
    export type HillshadeLayer = mapboxgl.HillshadeLayer;

    // Layout and paint
    export type AnyLayout = mapboxgl.AnyLayout;
    export type AnyPaint = mapboxgl.AnyPaint;
    export type BackgroundLayout = mapboxgl.BackgroundLayout;
    export type FillLayout = mapboxgl.FillLayout;
    export type LineLayout = mapboxgl.LineLayout;
    export type SymbolLayout = mapboxgl.SymbolLayout;
    export type CircleLayout = mapboxgl.CircleLayout;
    export type HeatmapLayout = mapboxgl.HeatmapLayout;
    export type FillExtrusionLayout = mapboxgl.FillExtrusionLayout;
    export type RasterLayout = mapboxgl.RasterLayout;
    export type HillshadeLayout = mapboxgl.HillshadeLayout;

    export type BackgroundPaint = mapboxgl.BackgroundPaint;
    export type FillPaint = mapboxgl.FillPaint;
    export type LinePaint = mapboxgl.LinePaint;
    export type SymbolPaint = mapboxgl.SymbolPaint;
    export type CirclePaint = mapboxgl.CirclePaint;
    export type HeatmapPaint = mapboxgl.HeatmapPaint;
    export type FillExtrusionPaint = mapboxgl.FillExtrusionPaint;
    export type RasterPaint = mapboxgl.RasterPaint;
    export type HillshadePaint = mapboxgl.HillshadePaint;

    // Layout types
    export type AnyLayout = mapboxgl.AnyLayout;
    export type BackgroundLayout = mapboxgl.BackgroundLayout;
    export type FillLayout = mapboxgl.FillLayout;
    export type LineLayout = mapboxgl.LineLayout;
    export type SymbolLayout = mapboxgl.SymbolLayout;
    export type CircleLayout = mapboxgl.CircleLayout;
    export type HeatmapLayout = mapboxgl.HeatmapLayout;
    export type FillExtrusionLayout = mapboxgl.FillExtrusionLayout;
    export type RasterLayout = mapboxgl.RasterLayout;
    export type HillshadeLayout = mapboxgl.HillshadeLayout;

    // Paint types
    export type BackgroundPaint = mapboxgl.BackgroundPaint;
    export type FillPaint = mapboxgl.FillPaint;
    export type LinePaint = mapboxgl.LinePaint;
    export type SymbolPaint = mapboxgl.SymbolPaint;
    export type CirclePaint = mapboxgl.CirclePaint;
    export type HeatmapPaint = mapboxgl.HeatmapPaint;
    export type FillExtrusionPaint = mapboxgl.FillExtrusionPaint;
    export type RasterPaint = mapboxgl.RasterPaint;
    export type HillshadePaint = mapboxgl.HillshadePaint;

    // Source types
    export type AnySourceData = mapboxgl.AnySourceData;
    export type AnySourceImpl = mapboxgl.AnySourceImpl;
    export type GeoJSONSourceRaw = mapboxgl.GeoJSONSourceRaw;
    export type GeoJSONSource = mapboxgl.GeoJSONSource;
    export type VectorSource = mapboxgl.VectorSource;
    export type RasterSource = mapboxgl.RasterSource;
    export type RasterDemSource = mapboxgl.RasterDemSource;
    export type VideoSource = mapboxgl.VideoSource;
    export type ImageSource = mapboxgl.ImageSource;
    export type CanvasSource = mapboxgl.CanvasSource;

    // GeoJSON types
    export type GeoJSONSourceOptions = mapboxgl.GeoJSONSourceOptions;
    export type GeoJSONSourceRaw = mapboxgl.GeoJSONSourceRaw;
    export type GeoJSONSource = mapboxgl.GeoJSONSource;

    // Expression types
    export type Expression = mapboxgl.Expression;
    export type StyleFunction = mapboxgl.StyleFunction;
    export type Transition = mapboxgl.Transition;

    // Filter types
    export type FilterSpecification = any[];

    // MapGeoJSONFeature
    export type MapGeoJSONFeature = mapboxgl.MapboxGeoJSONFeature;

    // LayerSpecification
    export type LayerSpecification = mapboxgl.AnyLayer;

    // GeoJSONSourceSpecification
    export type GeoJSONSourceSpecification = mapboxgl.GeoJSONSourceRaw;

    // FilterSpecification
    export type FilterSpecification = any[];
}