# ingredient-toxicity-analyzer
"A simple OCR + toxicity analysis tool that scans product ingredient lists and generates a toxicity score + safety verdict."

📦 Ingredient Toxicity Analyzer

A lightweight CLI tool that uses OCR + rule-based NLP to detect potentially harmful ingredients from product labels.

🔍 What it does

Extracts ingredient text from product images using Tesseract OCR

Cleans and normalizes the OCR output

Detects known harmful or allergenic chemicals

Generates:

Toxicity meters per-ingredient

Overall toxicity score

Final safety verdict (Safe / Caution / Avoid)

🧪 Demo
Toxic ingredients (severity meter 0–10):
Sodium laureth sulfate            6/10  ███████████·……
Methylisothiazolinone (MI)        9/10  ██████████████…

Overall toxicity score: 7.75 / 10  
Overall meter: ███████████████████····················  
Verdict: AVOID — High risk, especially for leave-on use.

🎯 PM Case Impact

As a PM, I built this tool to:

Understand how OCR + NLP pipelines work

Improve my ability to translate user problems into technical solutions

Demonstrate decision-making using severity scoring + heuristics

Explore safety in consumer products through data-backed signals

🛠 Tech Stack

Java

Tesseract OCR (CLI)

Regex-based NLP classification
