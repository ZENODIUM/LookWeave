# OpenCV opportunities for LookWeave

## Recommendation

Use OpenCV as an on-device **evidence capture layer**, not as a second virtual try-on engine. The strongest hackathon story is: YouCam renders appearance, while local computer vision checks whether the input is usable and measures repeatable interaction evidence without uploading the camera stream.

The best first implementation is **Guided Wardrobe Capture**: live framing, blur/exposure checks, garment/background separation, dominant-color extraction, and label/QR capture. It is useful, inexpensive, privacy-forward, and easy to demonstrate.

## High-value capabilities

| Capability | OpenCV / model | LookWeave use | Value | Caveat |
|---|---|---|---|---|
| Person/garment cutout | PP-HumanSeg or EfficientSAM + morphology/GrabCut | Clean wardrobe thumbnails and better VTO inputs | Very high | Human segmentation is not garment parsing; do not call it fit analysis. |
| Capture quality gate | Laplacian variance, histogram, exposure and contour checks | Stop blurry, dark, cropped, or tiny garment uploads before an API credit is spent | Very high | Calibrate thresholds across devices. |
| Dominant palette | Lab color space + k-means/histograms | Search by color, contrast-aware outfits, color naming | High | Lighting changes color; show as an estimate until confirmed. |
| Label and care-symbol reader | perspective correction + PPOCR/CRNN; optionally Gemini OCR after crop | Extract fabric, care and closure evidence from labels | High | OCR needs confirmation; care symbols need a dedicated classifier/dataset. |
| Barcode / QR intake | `QRCodeDetector`, WeChatQRCode, barcode module | Product lookup and provenance capture | High | A code identifies a product only when a catalog resolves it. |
| Closure detector | small custom YOLO/NanoDet ONNX model | Detect buttons, zippers, magnets, snaps and hook-and-loop regions | Very high / signature | Requires a licensed, representative fashion dataset and validation. |
| Hand-to-closure interaction | MP-PalmDet + MP-HandPose + optical flow | Time fastening attempts and estimate retries or reach path | Very high / signature | Treat as observed interaction evidence, never diagnosis or safety proof. |
| Seated silhouette evidence | MP-PersonDet + MP-Pose | Guide consistent seated/standing photos and compare bunching zones | High | 2D pose does not prove pressure, comfort, or fit. |
| Device-clearance overlay | segmentation + user-marked protected region | Flag likely overlap with a waist/torso access zone | Very high / signature | User must define the region; segmentation is approximate. |
| Fabric texture cues | Gabor/LBP/edge density + a small classifier | Offer low-sensory candidates or detect visible ribbing/pile | Medium | Images cannot establish softness, seam feel, allergens, or irritation. |
| Garment category/object detection | YOLOX, NanoDet, MobileNet-SSD through `dnn` | Auto-sort wardrobe captures and locate multiple garments | Medium-high | A fashion-trained model is needed for useful classes. |
| Multi-item capture | contours, connected components, detector + tracking | Scan several closet items in one camera sweep | Medium | Occlusion and similar colors reduce reliability. |
| Duplicate detection | perceptual hash, ORB/SIFT features, histogram similarity | Prevent duplicate wardrobe entries | Medium-high | Similar basics may be different garments; ask before merging. |
| Background/privacy cleanup | segmentation + blur/inpainting | Keep people and room details out of garment records | High | Inpainting quality varies. |
| Perspective/size reference | ArUco/ChArUco or known-size card + calibration | Estimate garment dimensions and consistent drape photos | Medium-high | User setup adds friction; measurements remain estimates. |
| Motion/stability guidance | optical flow, feature tracking, video stabilization | Accessible hands-free capture with an automatic shutter | High | Avoid requiring the user to hold a fixed pose for long. |
| Change detection | registered before/after frames, SSIM, contours | Show where a hem, closure, or alteration changed | Medium | Camera and lighting must be controlled. |
| Offline visual search | ORB/SIFT or DNN embeddings + nearest-neighbor index | “Find items like this” without sending photos to a server | Medium-high | Classic features match texture more than style; embeddings work better. |
| Outfit composition guard | segmentation + palette/contrast analysis | Detect low visual contrast, busy overlap, or hidden closures | Medium | Style is subjective; present observations, not verdicts. |
| Camera calibration / AR anchor | calibration, solvePnP, ArUco | Stable accessory or access-zone overlays | Medium | Not a replacement for body-aware VTO. |

## Useful OpenCV libraries and tools

- **Official Android AAR:** `org.opencv:opencv`; OpenCV documents Maven Central distribution from 4.9 onward and Android API 21+ for 4.9.
- **Core + Imgproc:** resize, warp, threshold, contours, morphology, gradients, color conversion, histograms, k-means, GrabCut and template matching.
- **DNN:** run ONNX models locally. OpenCV’s Android tutorial demonstrates the standard `readNet` / forward path.
- **OpenCV Zoo:** PP-HumanSeg, EfficientSAM, NanoDet, YOLOX, VitTrack, MP-PalmDet, MP-HandPose, MP-PersonDet, MP-Pose, PPOCR, CRNN and WeChatQRCode are directly relevant candidates. Check the license of each individual model.
- **Objdetect / barcode / QR:** fast product and label intake without a cloud call.
- **Features2D + photo:** ORB/SIFT matching, denoise, color correction, alignment and inpainting.
- **Video:** optical flow, background subtraction and tracking for hands-free capture or interaction timing.
- **ArUco / ChArUco (contrib where required):** reference scale, camera calibration and stable overlays.
- **G-API:** graph-based image pipelines when the capture path becomes complex.
- **FastCV integration:** an optional custom build can accelerate supported operations on newer Snapdragon devices; it is not a safe hackathon dependency because it is chipset-specific.

## What can make the project win

### 1. Adaptive Interaction Lens

Record a short, opt-in clip of the user fastening a garment. On-device hand landmarks and optical flow produce simple evidence: elapsed time, retries, hand used and user-confirmed effort. The agent then learns “this closure took 42 seconds” instead of assuming ability from a diagnosis. This is a much sharper story than another recommendation chatbot.

### 2. Credit Saver capture gate

Before any YouCam request, reject unusable images locally and explain exactly how to correct them: more light, step back, show the full garment, reduce blur, or use a simpler background. This visibly respects the 500-credit constraint and makes the integration reliable.

### 3. Device-clearance canvas

Let the user draw a private keep-clear zone once. Segmentation overlays it on outfit/garment photos and the agent avoids waistbands, pockets, closures or layers that appear to conflict. The saved information is a task preference, not a medical label.

### 4. Evidence Passport

Combine label OCR, product/QR data, visual estimates and user-observed interaction trials. Every recommendation shows which source supports each claim and which facts still need confirmation. This reinforces LookWeave’s core differentiation: adaptive wearability intelligence with accountable evidence.

## What not to claim

- A photograph or pose model cannot prove fit, pressure, comfort, reach, sensory safety or medical suitability.
- Human segmentation is not fashion parsing, and fashion parsing is not body measurement.
- Visible texture is not fabric composition or tactile feel.
- Face recognition, emotion recognition and disability inference add privacy risk without improving the product; exclude them.

## Practical implementation order

1. Capture quality gate and dominant palette (fast, model-free).
2. PP-HumanSeg cutout and privacy background cleanup.
3. Label/QR capture with confirmation.
4. Hand-to-closure interaction prototype.
5. Custom closure detector only after a suitable licensed dataset is available.

## Primary references

- [OpenCV Android development and official Maven artifact](https://docs.opencv.org/5.0/tutorials/introduction/android_binary_package/dev_with_OCV_on_Android.html)
- [Running DNN models on Android](https://docs.opencv.org/5.0/tutorials/introduction/android_binary_package/android_dnn_intro.html)
- [OpenCV Zoo models and benchmark project](https://github.com/opencv/opencv_zoo)
- [PP-HumanSeg model card](https://github.com/opencv/opencv_zoo/blob/main/models/human_segmentation_pphumanseg/README.md)
- [OpenCV DNN engine selection](https://docs.opencv.org/doc/doxygen/html/de/db7/api_dnn_engine_selection.html)
- [FastCV acceleration constraints](https://docs.opencv.org/4.12.0/d5/d06/tutorial_building_fastcv.html)
