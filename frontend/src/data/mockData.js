export const DOMAINS = [
  { id: 'all', name: 'All Domains' },
  { id: 'programming', name: 'Software & Engineering' },
  { id: 'mathematics', name: 'Mathematics & Data' },
  { id: 'languages', name: 'Languages & Linguistics' },
  { id: 'business', name: 'Business & Leadership' },
];

export const SUBJECTS = [
  {
    id: 'java-backend',
    domain: 'programming',
    title: 'Full-Stack Java & Spring Boot Architecture',
    tagline: 'Modern backend engineering, REST services, JPA, and Spring Security',
    difficulty: 'Intermediate',
    estimatedWeeks: 8,
    popular: true,
    skillsCovered: ['Java 21', 'Spring Boot 3', 'PostgreSQL', 'JPA/Hibernate', 'RESTful Design', 'JWT Security'],
  },
  {
    id: 'python-backend',
    domain: 'programming',
    title: 'Python Backend Systems & FastAPI',
    tagline: 'Asynchronous APIs, Pydantic, SQLAlchemy, and Docker deployment',
    difficulty: 'Beginner to Intermediate',
    estimatedWeeks: 6,
    popular: true,
    skillsCovered: ['Python 3.12', 'FastAPI', 'AsyncIO', 'SQLAlchemy', 'Pydantic', 'Pytest'],
  },
  {
    id: 'linear-algebra',
    domain: 'mathematics',
    title: 'Linear Algebra for Machine Learning',
    tagline: 'Vector spaces, matrix decomposition, eigenvalues, and SVD applied to ML',
    difficulty: 'Intermediate',
    estimatedWeeks: 5,
    popular: true,
    skillsCovered: ['Vector Spaces', 'Eigenvalues', 'Matrix Decomposition', 'SVD', 'Principal Component Analysis'],
  },
  {
    id: 'calculus',
    domain: 'mathematics',
    title: 'Multivariable Calculus & Optimization',
    tagline: 'Gradients, Hessians, partial derivatives, and Lagrange multipliers',
    difficulty: 'Advanced',
    estimatedWeeks: 6,
    popular: false,
    skillsCovered: ['Partial Derivatives', 'Gradients', 'Jacobian & Hessian', 'Gradient Descent Foundations'],
  },
  {
    id: 'spanish',
    domain: 'languages',
    title: 'Conversational Spanish (CEFR A1–B1)',
    tagline: 'Everyday dialogue, grammar essentials, verb conjugations, and active recall',
    difficulty: 'Beginner',
    estimatedWeeks: 7,
    popular: true,
    skillsCovered: ['Present & Past Tenses', 'Essential Vocabulary', 'Audio Fluency', 'Grammar Conjugation'],
  },
  {
    id: 'product-mgmt',
    domain: 'business',
    title: 'Product Management & Strategic Roadmapping',
    tagline: 'Discovery, customer research, North Star metrics, and RICE prioritization',
    difficulty: 'All Levels',
    estimatedWeeks: 4,
    popular: false,
    skillsCovered: ['User Research', 'Product Strategy', 'KPIs & North Star', 'Agile Product Backlogs'],
  },
];

export const DIAGNOSTIC_QUESTIONS = {
  'java-backend': [
    {
      id: 1,
      question: 'In Java OOP, which concept allows a subclass to provide a specific implementation of a method declared in its superclass?',
      options: [
        'Method Overloading',
        'Method Overriding (@Override)',
        'Data Encapsulation',
        'Object Serialization'
      ],
      correctAnswer: 1,
      skill: 'Core Java OOP',
      explanation: 'Method overriding occurs when a subclass defines a method with the exact same signature and return type as in its parent class.'
    },
    {
      id: 2,
      question: 'In the Spring Framework, what does Inversion of Control (IoC) primarily achieve?',
      options: [
        'It speeds up database query execution',
        'It delegates object creation and dependency injection to the Spring IoC container',
        'It converts Java bytecode into native machine instructions',
        'It automatically encrypts network communications'
      ],
      correctAnswer: 1,
      skill: 'Spring Core & IoC',
      explanation: 'IoC transfers the responsibility of managing object lifecycles and dependencies to the framework container.'
    },
    {
      id: 3,
      question: 'Which HTTP method should be used for idempotent updates where the entire representation of a resource is replaced?',
      options: ['POST', 'GET', 'PUT', 'PATCH'],
      correctAnswer: 2,
      skill: 'REST API Design',
      explanation: 'PUT is idempotent and replaces the entire target resource; PATCH is typically used for partial modifications.'
    },
    {
      id: 4,
      question: 'What is the "N+1 query problem" in JPA / Hibernate ORM?',
      options: [
        'Running N queries inside a single database transaction',
        'Executing 1 initial query to fetch N entities, followed by N separate queries to fetch related child entities',
        'A constraint violation when inserting N+1 rows with duplicate primary keys',
        'Having N+1 threads attempting to write to the same table simultaneously'
      ],
      correctAnswer: 1,
      skill: 'JPA & Database Performance',
      explanation: 'The N+1 problem happens when lazy loading fetches a parent list with 1 query, then fires N additional queries for each child record.'
    },
    {
      id: 5,
      question: 'Why are JSON Web Tokens (JWT) commonly favored in scalable microservices architectures?',
      options: [
        'They eliminate the need for HTTPS',
        'They are stateless and self-contained, allowing services to verify authentication without querying a shared session store',
        'They automatically encrypt the entire request payload',
        'They prevent all Cross-Site Scripting (XSS) vulnerabilities'
      ],
      correctAnswer: 1,
      skill: 'Security & Authentication',
      explanation: 'JWTs carry verifiable claims signed cryptographically, allowing stateless verification across distributed services.'
    },
  ],
  default: [
    {
      id: 1,
      question: 'What is your current baseline familiarity with foundational terminology in this discipline?',
      options: [
        'Complete Beginner: I am starting from absolute scratch',
        'Novice: I have browsed introductory tutorials or articles',
        'Intermediate: I have applied core concepts in small projects or coursework',
        'Experienced: I know the fundamentals and want advanced specialization'
      ],
      correctAnswer: 2,
      skill: 'Foundational Knowledge',
      explanation: 'Helps determine whether introductory prerequisites can be accelerated.'
    },
    {
      id: 2,
      question: 'When encountering an unfamiliar problem in this domain, how do you typically approach finding a solution?',
      options: [
        'I need guided step-by-step instructions from zero',
        'I understand the high-level pattern but need syntax or formula references',
        'I can independently debug and decompose the problem into sub-units',
        'I evaluate multiple trade-offs and edge cases systematically'
      ],
      correctAnswer: 2,
      skill: 'Problem Decomposition',
      explanation: 'Measures conceptual independence vs guided dependency.'
    },
    {
      id: 3,
      question: 'How comfortable are you with prerequisite analytical or formal reasoning in this topic?',
      options: [
        'Uncomfortable, I prefer practical applied examples first',
        'Moderate, I understand basic mathematical/structural rules',
        'Strong, I can read formal specifications or technical documentation',
        'Advanced, I can construct formal proofs or architectural blueprints'
      ],
      correctAnswer: 1,
      skill: 'Analytical Reasoning',
      explanation: 'Calibrates whether lessons lead with practical analogies or rigorous specs.'
    },
    {
      id: 4,
      question: 'What is your primary goal for this specific skill?',
      options: [
        'General curiosity and intellectual exploration',
        'Academic examination or university course preparation',
        'Building a tangible personal or open-source project',
        'Career transition, interview readiness, or professional promotion'
      ],
      correctAnswer: 3,
      skill: 'Outcome Orientation',
      explanation: 'Drives milestone milestones and project requirements in the generated roadmap.'
    },
    {
      id: 5,
      question: 'How do you best retain complex information over time?',
      options: [
        'Reading conceptual summaries repeatedly',
        'Interactive micro-quizzes and active recall flashcards',
        'Writing code or solving concrete numerical exercises immediately',
        'Explaining the concept in simple terms (Feynman Technique)'
      ],
      correctAnswer: 2,
      skill: 'Learning Velocity',
      explanation: 'Configures practice-to-theory ratios in your dashboard.'
    },
  ],
};

export const MOCK_SKILL_GAPS = {
  'java-backend': {
    readinessScore: 58,
    mastered: [
      { name: 'Core Java Syntax & Control Flow', level: 'Mastered', tag: 'Prerequisite' },
      { name: 'Basic Object-Oriented Principles', level: 'Mastered', tag: 'Prerequisite' },
      { name: 'Collections Framework (Lists & Maps)', level: 'Mastered', tag: 'Prerequisite' },
    ],
    gaps: [
      { name: 'Spring Dependency Injection (IoC)', severity: 'High Gap', reason: 'Prerequisite for building Spring Boot microservices' },
      { name: 'JPA / Hibernate Relationship Mappings', severity: 'High Gap', reason: 'Required to prevent N+1 queries and handle data transactions' },
      { name: 'Stateless Authentication (JWT + Spring Security)', severity: 'Medium Gap', reason: 'Essential for securing endpoints' },
      { name: 'REST API Best Practices (Error DTOs & Status Codes)', severity: 'Low Gap', reason: 'Needed for professional API design' },
    ],
    curatedRecommendation: 'Based on your diagnostic, we will skip introductory Java syntax and fast-track directly to Spring Core and Database Persistence.',
  },
  default: {
    readinessScore: 50,
    mastered: [
      { name: 'Core Intuition & High-Level Terminology', level: 'Mastered', tag: 'Prerequisite' },
      { name: 'Basic Problem-Solving Discipline', level: 'Mastered', tag: 'Prerequisite' },
    ],
    gaps: [
      { name: 'Systematic Frameworks & Principles', severity: 'High Gap', reason: 'Core methodology needed to master advanced topics' },
      { name: 'Practical Hands-on Application', severity: 'High Gap', reason: 'Bridging theory to real-world projects' },
      { name: 'Edge Case Evaluation & Optimization', severity: 'Medium Gap', reason: 'Crucial for achieving true professional competence' },
    ],
    curatedRecommendation: 'Your roadmap will condense introductory theory into high-impact diagnostic checkpoints, prioritizing hands-on exercises.',
  },
};

export const MOCK_ROADMAPS = {
  'java-backend': [
    {
      id: 'mod-1',
      title: 'Module 1: Spring Core & Dependency Injection',
      duration: 'Week 1-2',
      status: 'unlocked', // 'unlocked' | 'locked' | 'completed'
      isCurrent: true,
      description: 'Understand the Spring container, Bean lifecycles, and @Autowired vs constructor injection.',
      lessons: [
        { id: 'l1', title: 'Why Inversion of Control matters', completed: true, type: 'Theory + Code' },
        { id: 'l2', title: 'Spring Beans & ApplicationContext', completed: true, type: 'Hands-on Lab' },
        { id: 'l3', title: 'Constructor Injection vs Field Injection', completed: false, type: 'Best Practice' },
        { id: 'l4', title: 'Assessment: IoC Mastery Test', completed: false, type: 'Quiz' },
      ],
      prerequisite: 'Core Java OOP (Verified)',
    },
    {
      id: 'mod-2',
      title: 'Module 2: Building RESTful APIs with Spring Web',
      duration: 'Week 3',
      status: 'unlocked',
      isCurrent: false,
      description: 'Build production-ready controllers, request validation, global exception handlers, and DTO patterns.',
      lessons: [
        { id: 'l5', title: '@RestController and Request Mappings', completed: false, type: 'Hands-on Lab' },
        { id: 'l6', title: 'DTOs, Validation & Global @ControllerAdvice', completed: false, type: 'Architecture' },
        { id: 'l7', title: 'HTTP Status Code Conventions', completed: false, type: 'Standard' },
      ],
      prerequisite: 'Module 1: Spring Core',
    },
    {
      id: 'mod-3',
      title: 'Module 3: PostgreSQL Persistence with Spring Data JPA',
      duration: 'Week 4-5',
      status: 'locked',
      isCurrent: false,
      description: 'Connect to PostgreSQL, model entities, manage transactions, and eliminate N+1 queries.',
      lessons: [
        { id: 'l8', title: 'PostgreSQL connection & DataSource configuration', completed: false, type: 'Setup' },
        { id: 'l9', title: '@Entity, @Table, and Repository interfaces', completed: false, type: 'Hands-on Lab' },
        { id: 'l10', title: 'FetchType.LAZY vs EAGER & JOIN FETCH', completed: false, type: 'Optimization' },
      ],
      prerequisite: 'Module 2: RESTful APIs',
    },
    {
      id: 'mod-4',
      title: 'Module 4: Authentication & Security with JWT',
      duration: 'Week 6-7',
      status: 'locked',
      isCurrent: false,
      description: 'Implement stateless JWT token generation, security filter chains, and role-based access control.',
      lessons: [
        { id: 'l11', title: 'Spring Security Filter Chain Architecture', completed: false, type: 'Deep Dive' },
        { id: 'l12', title: 'JWT Token Minting & Verification', completed: false, type: 'Security' },
        { id: 'l13', title: 'Password Hashing with BCrypt', completed: false, type: 'Practice' },
      ],
      prerequisite: 'Module 3: Spring Data JPA',
    },
    {
      id: 'mod-5',
      title: 'Module 5: Portfolio Capstone Project',
      duration: 'Week 8',
      status: 'locked',
      isCurrent: false,
      description: 'Design and deploy an end-to-end production API with test coverage, Docker containerization, and docs.',
      lessons: [
        { id: 'l14', title: 'System Design & ERD Specification', completed: false, type: 'Project' },
        { id: 'l15', title: 'Full Stack Integration & Public Deployment', completed: false, type: 'Capstone' },
      ],
      prerequisite: 'Modules 1 through 4',
    },
  ],
  default: [
    {
      id: 'mod-1',
      title: 'Module 1: Core Principles & Diagnostic Foundations',
      duration: 'Week 1',
      status: 'unlocked',
      isCurrent: true,
      description: 'Targeted overview of foundational building blocks, closing prerequisite gaps identified in your test.',
      lessons: [
        { id: 'l1', title: 'Core Conceptual Framework', completed: true, type: 'Theory' },
        { id: 'l2', title: 'Interactive Practice: Core Principles', completed: false, type: 'Exercise' },
      ],
      prerequisite: 'Diagnostic Baseline',
    },
    {
      id: 'mod-2',
      title: 'Module 2: Applied Methodologies & Structural Patterns',
      duration: 'Week 2-3',
      status: 'unlocked',
      isCurrent: false,
      description: 'Hands-on practice applying the discipline to realistic problems and case studies.',
      lessons: [
        { id: 'l3', title: 'Standard Industry Patterns', completed: false, type: 'Hands-on' },
        { id: 'l4', title: 'Diagnostic Checkpoint Quiz', completed: false, type: 'Quiz' },
      ],
      prerequisite: 'Module 1',
    },
    {
      id: 'mod-3',
      title: 'Module 3: Advanced Topics & Mastery Capstone',
      duration: 'Week 4-5',
      status: 'locked',
      isCurrent: false,
      description: 'Synthesis of all modules into an independent, portfolio-ready project or exam simulation.',
      lessons: [
        { id: 'l5', title: 'Synthesis & Real-World Edge Cases', completed: false, type: 'Advanced' },
        { id: 'l6', title: 'Comprehensive Mastery Assessment', completed: false, type: 'Capstone' },
      ],
      prerequisite: 'Module 2',
    },
  ],
};

export const SAMPLE_PRACTICE_QUIZ = {
  title: 'Quick Practice Quiz: Spring IoC & Dependency Injection',
  questions: [
    {
      id: 'q1',
      text: 'Why is constructor injection generally preferred over field injection with @Autowired in Spring?',
      options: [
        'It produces faster CPU execution at runtime',
        'It makes dependencies immutable, explicit, and easy to mock in unit tests without Spring',
        'Field injection is deprecated in Java 21',
        'Constructor injection uses less RAM memory'
      ],
      correct: 1,
      explanation: 'Constructor injection ensures required dependencies cannot be null, supports immutability (final fields), and allows easy plain unit testing.'
    },
    {
      id: 'q2',
      text: 'What is the default bean scope in Spring ApplicationContext?',
      options: ['prototype', 'singleton', 'request', 'session'],
      correct: 1,
      explanation: 'Spring beans are singletons by default; exactly one shared instance is managed per container.'
    }
  ]
};
