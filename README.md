# ProjetFosyma
 Rappel du planning prévisionnel du projet 
 - Semaine 2 (8 f évrier):    Introduction `a JADE
 - Semaine 3 (15 février):    Introduction au projet, exploration multi-agent
 - Semaine 4 (1 mars):    Partage d’information entre les agents
 - Semaine 5 (8 mars):    Formation de coalitions et isolation d’un adversaire sur des arbres
 - Semaine 6 (22 mars):    Isolation d’un unique adversaire sur un graphe quelconque
 - Semaine 7 (29 mars):    Gestion des interblocages
 - Semaine 8 (5 avril):    Gestion fine des communications et multiples adversaires
 - Semaine 9 (12 avril):    Distribution du SMA sur diff ́erentes machines
 - Semaine 10 (3 mai):    Soutenances


## travail au courant:
 implémenter la chasse et le blocage d'un adversaire. Réfléchir à  : 
 
 --> L'impact de la topologie sur la chasse
 
 --> La possibilité (ou pas) de démarrer la chasse avant la fin de l'exploration
 
 --> Les informations a retenir pour pouvoir réaliser une chasse efficace
 
 --> La minimisation du nombre d'agents nécessaires dans votre équipe pour réussir votre chasse
 
 --> L'impact de la présence éventuelle de plusieurs adversaires sur votre stratégie
 
 ## travail quasi-reussi:
  - Faire que vos agents obtiennent la liste des agents ailleurs que lors de la création (que ce soit via l'ams ou les pages-jaunes)
 - Optimiser la stratégie d'exploration : Dans la version fournie ils vont rapidement se suivre dès qu'ils ont partagé leur carte. Ce n'est clairement pas efficace.
 - Optimiser le partage d'information : Dans la version fournie, ils partagent toute la carte tout le temps. La périodicité et le contenu peuvent être grandement amélioré
