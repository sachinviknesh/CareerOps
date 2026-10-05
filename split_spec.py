import os
import sys

with open('spec_extracted.txt', 'r', encoding='utf-8') as f:
    text = f.read()

keywords = [
    '# 4. KEEP EXACTLY 4 JPA ENTITY CLASSES',
    '# 5. JPA RELATIONSHIPS',
    '# 6. PACKAGE STRUCTURE',
    '# 8. CONFIGURATION',
    '# 9. AUTHENTICATION',
    '# 10. CRUD REQUIREMENTS',
    '# 11. JPA DERIVED QUERIES',
    '# 12. JPQL QUERIES',
    '# 13. PAGINATION + SORTING',
    '# 14. RESUME FEATURE',
    '# 15. OFFLINE RESUME QUALITY ANALYSIS',
    '# 16. JOB-FIT FEATURE',
    '# 17. OFFLINE JOB-FIT ALGORITHM',
    '# 18. FIT RESULT',
    '# 20. AI FEATURE',
    '# 21. AI PROVIDER DESIGN',
    '# 22. AI REVIEW FEATURES',
    '# 25. APPLICATION TRACKER FLOW',
    '# 26. APPLICATION API EXAMPLE',
    '# 27. VALIDATION',
    '# 28. EXCEPTION HANDLING',
    '# 30. SWAGGER / OPENAPI',
    '# 31. FRONTEND',
    '# 32. FRONTEND PAGES',
    '# 45. API RESPONSE STYLE',
    '# 48. DATABASE SCHEMA',
    '# 49. SECURITY RULES',
    '# 50. DUPLICATE APPLICATION RULE',
    '# 53. POSTMAN',
    '# 54. AUTOMATED TESTS',
    '# 55. DOCUMENTATION',
    '# 56. ENTITY DIAGRAM',
    '# 57. README MUST EXPLAIN',
    '# 58. OFFLINE / ONLINE FEATURE MATRIX',
    '# 60. AI RESPONSE MUST BE STRUCTURED',
    '# 65. DEMO SCENARIO',
    '# 68. IMPLEMENTATION ORDER',
    '# 70. FINAL QUALITY GATE'
]

os.makedirs('spec_sections', exist_ok=True)
for i, kw in enumerate(keywords):
    pos = text.find(kw)
    if pos != -1:
        next_pos = len(text)
        if i + 1 < len(keywords):
            np = text.find(keywords[i+1], pos + len(kw))
            if np != -1:
                next_pos = np
        sec_content = text[pos:next_pos]
        safe_name = kw.replace('#', '').strip().replace(' ', '_').replace('/', '_').replace('.', '_')[:40]
        with open(f'spec_sections/{i:02d}_{safe_name}.txt', 'w', encoding='utf-8') as out:
            out.write(sec_content)

print("Saved spec sections!")
