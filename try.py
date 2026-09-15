"""
Real-time webcam image classification (PyTorch version)
Libraries needed:
    pip install torch torchvision opencv-python numpy

Run:
    python webcam_classifier_torch.py
Press 'q' to quit.
"""

import cv2
import torch
from torchvision.models import mobilenet_v2, MobileNet_V2_Weights

# --- Load pretrained model (trained on ImageNet, 1000 classes) ---
print("Loading model... (first run downloads weights, ~14MB)")
weights = MobileNet_V2_Weights.IMAGENET1K_V1
model = mobilenet_v2(weights=weights)
model.eval()  # inference mode, not training

# The weights object comes with the correct preprocessing pipeline
# (resize, normalize, etc.) AND the class name list — no extra files needed.
preprocess = weights.transforms()
categories = weights.meta["categories"]

# Use GPU if available, otherwise CPU
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model.to(device)
print(f"Using device: {device}")

# --- Open the webcam (0 = default camera) ---
cap = cv2.VideoCapture(0)
if not cap.isOpened():
    raise RuntimeError("Could not open webcam. Try changing 0 to 1.")

while True:
    ret, frame = cap.read()
    if not ret:
        break

    # --- Preprocess the frame for the model ---
    img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)  # OpenCV uses BGR, model wants RGB
    img_tensor = preprocess(torch.from_numpy(img_rgb).permute(2, 0, 1))
    input_batch = img_tensor.unsqueeze(0).to(device)

    # --- Predict ---
    with torch.no_grad():
        output = model(input_batch)
        probs = torch.nn.functional.softmax(output[0], dim=0)
        confidence, class_id = torch.max(probs, dim=0)

    label = categories[class_id.item()]
    text = f"{label}: {confidence.item() * 100:.1f}%"

    # --- Draw the result on the frame ---
    cv2.putText(
        frame, text, (20, 40),
        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2
    )

    cv2.imshow("Webcam Classification (press q to quit)", frame)

    if cv2.waitKey(1) & 0xFF == ord("q"):
        break

cap.release()
cv2.destroyAllWindows()