package com.indigonauts.gome.common;

public abstract class QuickSortable {

  public static void quicksort(QuickSortable[] a) {
    quicksort(a, 0, a.length - 1);
  }

  private static void quicksort(QuickSortable[] a, int left, int right) {
    if (right <= left)
      return;
    int i = partition(a, left, right);
    quicksort(a, left, i - 1);
    quicksort(a, i + 1, right);
  }

  private static int partition(QuickSortable[] a, int left, int right) {
    int i = left - 1;
    int j = right;
    while (true) {
      while (a[++i].lessThan(a[right]))
        // find item on left to swap
        ; // a[right] acts as sentinel
      while (a[right].lessThan(a[--j]))
        // find item on right to swap
        if (j == left)
          break; // don't go out-of-bounds
      if (i >= j)
        break; // check if pointers cross
      exch(a, i, j); // swap two elements into place
    }
    exch(a, i, right); // swap with partition element
    return i;
  }

  

  // exchange a[i] and a[j]
  private static void exch(QuickSortable[] a, int i, int j) {
    QuickSortable swap = a[i];
    a[i] = a[j];
    a[j] = swap;
  }
  
  public abstract boolean lessThan(QuickSortable other);
  
}
