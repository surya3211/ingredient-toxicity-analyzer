# Ingredient-toxicity-analyzer

A lightweight OCR + NLP tool that analyzes product ingredient lists and instantly tells users how safe a product is.

## WHAT IT DOES

• Extracts ingredient text from product images (Tesseract OCR)  
• Cleans & normalizes messy OCR text  
• Detects common harmful / allergenic ingredients  
• Generates:  
  - Per-ingredient toxicity meters  
  - Overall toxicity score (0–10)  
  - Final verdict: Safe / Caution / Avoid 

---


## Demo

Toxic ingredients:

Sodium laureth sulfate        6/10 ███████████····················

Methylisothiazolinone (MI)    9/10 ███████████████████···········

Overall score: 7.75 / 10

Verdict: AVOID — High risk, especially for leave-on use.

---

## WHY THIS PROJECT

• Built to practice product thinking + technical depth  
• Turning a user pain point into a working prototype  
• Creating a simple scoring model to support clear decisions  
• Demonstrating rapid prototyping and problem-solving  

---

## TECH STACK

• Java  
• Tesseract OCR (CLI)  
• Regex-based NLP detection  
• ASCII toxicity meters + color coding  
