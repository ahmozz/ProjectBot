:- use_module(library(jpl)).
:- use_module(library(when)).

init(I) :-
  jpl_call('com.mycompany.labopart4.HunterBot', 'getBotInstance', [], I).


run(X) :-
    (   should_engage(X)
    ->  engage(X)
    ;   true
    ),
    (   should_stop_shooting(X)
    ->  stop_shooting(X)
    ;   true
    ),
    (   being_shot(X)
    ->  turnAroundToFindEnemy(X)
    ;   true
    ),
    (   enemyToPursue(X)
    ->  pursueEnemy(X)
    ;   true
    ),
    (   beingSrlyInjured(X), not(enemyToPursue(X)), not(being_shot(X)), not(engage(X))
    ->  findMedKit(X)
    ;   true
    ).

engage(X) :-
  write('CA MARCHE'),
  %init(I),
  %should_engage(X,R),
  jpl_call(X, 'stateEngage', [], R).

stop_shooting(X) :-
  jpl_call(X, 'getAct', [], A),
  jpl_new('cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting', [], O),
  jpl_call(A, 'act', [O], R).

turnAroundToFindEnemy(X) :-
  jpl_call(X, 'stateHit', [], R).

pursueEnemy(X) :-
  jpl_call(X, 'statePursue', [], R).


findMedKit(X) :-
  jpl_call(X, 'stateMedKit', [], R).

beingSrlyInjured(X) :-
  jpl_call(X, 'getInfo', [], I),
  jpl_call(I, 'getHealth', [], H),
  jpl_get(X, 'shouldCollectHealth', SCH),
  jpl_is_true(SCH).

enemyToPursue(X) :-
  jpl_get(X, 'enemy', E),
  not(jpl_null(E)),
  jpl_get(X, 'shouldPursue', SP),
  jpl_is_true(SP),
  jpl_call(X, 'getWeaponry', [], W),
  jpl_call(W, 'hasLoadedWeapon', [], L),
  jpl_is_true(L).

being_shot(X) :-
  jpl_call(X, 'getSenses', [], S),
  jpl_call(S, 'isBeingDamaged', [], P),
  jpl_is_true(P).

should_engage(X) :-
  jpl_get(X, 'shouldEngage', V),
  jpl_is_true(V),
  jpl_call(X, 'getPlayers', [], P),
  jpl_call(P, 'canSeeEnemies', [], E),
  jpl_is_true(E),
  jpl_call(X, 'getWeaponry', [], W),
  jpl_call(W, 'hasLoadedWeapon', [], L),
  jpl_is_true(L).

should_stop_shooting(X) :-
  jpl_call(X, 'getInfo', [], I),
  jpl_call(I, 'isShooting', [], RP),
  jpl_is_true(RP),
  not(should_engage(X)).

should_stop_shooting(X) :-
  jpl_call(X, 'getInfo', [], I),
  jpl_call(I, 'isSecondaryShooting', [], RS),
  jpl_is_true(RS).


%J = jpl_get(J, 'shouldEngage', V),


%jpl_call(J, 'stateEngage', [], R).


%when(should_engage(X), call_java(X)).

close :-
    halt.

t :-
    throw( 'this is an error message').

display(X) :-
    write( X).
